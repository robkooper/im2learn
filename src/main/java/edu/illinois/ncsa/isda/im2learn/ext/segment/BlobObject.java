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

import java.io.RandomAccessFile;

import edu.illinois.ncsa.isda.im2learn.core.datatype.LimitValues;


/**
 * @author yjlee
 * 
 */
public class BlobObject {
	// statistics of blob.
	protected double 	meanRow;
	protected double	meanCol;
	protected double	sDevRow;
	protected double	sDevCol;
	protected double	varRow;
	protected double	varCol;
	protected double	cvar;
	protected double	cor;
	protected double	slope;
	protected double	spread;
	protected double	elng;
	protected double	orien;
	protected int		perim;
	
	  public int size;
	  public long sumi;
	  public long sumj;
	  public float sumi2;
	  public float sumj2;
	  public float sumij;

	// classes
	protected LimitValues lim = new LimitValues();

	public BlobObject() {
		resetBlobObject();
	}

	public void resetBlobObject() {
		meanRow = meanCol = 0.0;
		sDevRow = sDevCol = 0.0;
		varRow = varCol = 0.0;
		cvar = cor = slope = spread = elng = orien = 0.0;
		perim = 0;
	}

	// output methods
	public void PrintBlobStats(String OutFileName) throws Exception {
		// open the file
		RandomAccessFile f0;
		f0 = new RandomAccessFile(OutFileName, "rw");

		f0.writeBytes("blob stats ");
		f0.writeBytes(" \n");
		f0.writeBytes(" perim= ");
		f0.writeBytes(Integer.toString((int) getPerim()));

		f0.writeBytes("\n meanRow= ");
		f0.writeBytes(Float.toString((float) getMeanRow()));
		f0.writeBytes("\n meanCol= ");
		f0.writeBytes(Float.toString((float) getMeanCol()));

		f0.writeBytes("\n stdevRow= ");
		f0.writeBytes(Float.toString((float) getSDevRow()));
		f0.writeBytes("\n stdevCol= ");
		f0.writeBytes(Float.toString((float) getSDevCol()));

		f0.writeBytes("\n varRow= ");
		f0.writeBytes(Float.toString((float) getVarRow()));
		f0.writeBytes("\n varCol= ");
		f0.writeBytes(Float.toString((float) getVarCol()));

		f0.writeBytes("\n cvar= ");
		f0.writeBytes(Float.toString((float) getCvar()));
		f0.writeBytes("\n cor= ");
		f0.writeBytes(Float.toString((float) getCor()));
		f0.writeBytes("\n slope= ");
		f0.writeBytes(Float.toString((float) slopeRad2Deg(getSlope())));
		f0.writeBytes("\n orien= ");
		f0.writeBytes(Float.toString((float) getOrien()));

		f0.writeBytes("\n spread= ");
		f0.writeBytes(Float.toString((float) getSpread()));
		f0.writeBytes("\n elng= ");
		f0.writeBytes(Float.toString((float) getElng()));

		f0.close();
	}

	public String BlobStats2String() {

		String ret = new String();
		ret = "Stats ";
		ret += " \n";
		ret += " perim= ";
		ret += Integer.toString((int) getPerim());

		ret += "\n meanRow= ";
		ret += Float.toString((float) getMeanRow());
		ret += "\n meanCol= ";
		ret += Float.toString((float) getMeanCol());

		ret += "\n stdevRow= ";
		ret += Float.toString((float) getSDevRow());
		ret += "\n stdevCol= ";
		ret += Float.toString((float) getSDevCol());

		ret += "\n varRow= ";
		ret += Float.toString((float) getVarRow());
		ret += "\n varCol= ";
		ret += Float.toString((float) getVarCol());

		ret += "\n cvar= ";
		ret += Float.toString((float) getCvar());
		ret += "\n cor= ";
		ret += Float.toString((float) getCor());
		ret += "\n slope= ";
		ret += Float.toString((float) slopeRad2Deg(getSlope()));
		ret += "\n orien= ";
		ret += Float.toString((float) getOrien());

		ret += "\n spread= ";
		ret += Float.toString((float) getSpread());
		ret += "\n elng= ";
		ret += Float.toString((float) getElng());

		return ret;
	}

	public double slopeRad2Deg(double val) {
		if (val != LimitValues.SLOPE_MAX) {
			return Math.toDegrees(Math.atan(val));
		} else {
			return (90.0);
		}
	}
	
	/**
	 * @return Returns the cor.
	 */
	public double getCor() {
		return cor;
	}
	/**
	 * @param cor The cor to set.
	 */
	public void setCor(double cor) {
		this.cor = cor;
	}
	/**
	 * @return Returns the cvar.
	 */
	public double getCvar() {
		return cvar;
	}
	/**
	 * @param cvar The cvar to set.
	 */
	public void setCvar(double cvar) {
		this.cvar = cvar;
	}
	/**
	 * @return Returns the elng.
	 */
	public double getElng() {
		return elng;
	}
	/**
	 * @param elng The elng to set.
	 */
	public void setElng(double elng) {
		this.elng = elng;
	}
	/**
	 * @return Returns the meanCol.
	 */
	public double getMeanCol() {
		return meanCol;
	}
	/**
	 * @param meanCol The meanCol to set.
	 */
	public void setMeanCol(double meanCol) {
		this.meanCol = meanCol;
	}
	/**
	 * @return Returns the meanRow.
	 */
	public double getMeanRow() {
		return meanRow;
	}
	/**
	 * @param meanRow The meanRow to set.
	 */
	public void setMeanRow(double meanRow) {
		this.meanRow = meanRow;
	}
	/**
	 * @return Returns the orien.
	 */
	public double getOrien() {
		return orien;
	}
	/**
	 * @param orien The orien to set.
	 */
	public void setOrien(double orien) {
		this.orien = orien;
	}
	/**
	 * @return Returns the perim.
	 */
	public int getPerim() {
		return perim;
	}
	/**
	 * @param perim The perim to set.
	 */
	public void setPerim(int perim) {
		this.perim = perim;
	}
	/**
	 * @return Returns the sdevCol.
	 */
	public double getSDevCol() {
		return sDevCol;
	}
	/**
	 * @param sdevCol The sdevCol to set.
	 */
	public void setSDevCol(double sdevCol) {
		this.sDevCol = sdevCol;
	}
	/**
	 * @return Returns the sdevRow.
	 */
	public double getSDevRow() {
		return sDevRow;
	}
	/**
	 * @param sdevRow The sdevRow to set.
	 */
	public void setSDevRow(double sdevRow) {
		this.sDevRow = sdevRow;
	}

	/**
	 * @return Returns the slope.
	 */
	public double getSlope() {
		return slope;
	}
	/**
	 * @param slope The slope to set.
	 */
	public void setSlope(double slope) {
		this.slope = slope;
	}
	/**
	 * @return Returns the spread.
	 */
	public double getSpread() {
		return spread;
	}
	/**
	 * @param spread The spread to set.
	 */
	public void setSpread(double spread) {
		this.spread = spread;
	}
	/**
	 * @return Returns the varCol.
	 */
	public double getVarCol() {
		return varCol;
	}
	/**
	 * @param varCol The varCol to set.
	 */
	public void setVarCol(double varCol) {
		this.varCol = varCol;
	}
	/**
	 * @return Returns the varRow.
	 */
	public double getVarRow() {
		return varRow;
	}
	/**
	 * @param varRow The varRow to set.
	 */
	public void setVarRow(double varRow) {
		this.varRow = varRow;
	}
}
