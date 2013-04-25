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
package edu.illinois.ncsa.isda.im2learn.core.datatype;

import java.io.Serializable;

/**
 * @author yjlee
 *
 */
public class LimitValues implements Serializable {
	/**
	 * @deprecated Use Byte.MIN_VALUE
	 */
	public static final byte MIN_BYTE = -128;

	/**
	 * @deprecated Use Byte.Max_VALUE
	 */
	public static final byte MAX_BYTE = 127;

	public static final int MAXPOS_BYTE = 256;

	/**
	 * @deprecated Use Short.MIN_VALUE
	 */
	public static final short MIN_SHORT = -32768;

	/**
	 * @deprecated Use Short.MAX_VALUE
	 */
	public static final short MAX_SHORT = 32767;

	public static final int MAXPOS_SHORT = 65536;

	/**
	 * @deprecated Use Integer.MIN_VALUE
	 */
	public static final int MIN_INT = -2147483648;

	/**
	 * @deprecated Use Integer.MAX_VALUE
	 */
	public static final int MAX_INT = 2147483647;

	public static final long MAXPOS_INT = 4294967296L; // 2147483648*2;

	/**
	 * @deprecated Use Long.MIN_VALUE
	 */
	public static final long MIN_LONG = -9223372036854775808L;

	/**
	 * @deprecated Use Long.MAX_VALUE
	 */
	public static final long MAX_LONG = 9223372036854775807L;

	public static final float MAXPOS_LONG = 1.8446744073709551615E+19F;

	/**
	 * @deprecated Use Float.MIN_VALUE
	 */
	public static final float SMALL_FLOAT = 3.40282347E-37F;

	/**
	 * @deprecated Use Float.MAX_VALUE
	 */
	public static final float MAX_FLOAT = 3.40282347E+38F;

	public static final float MIN_FLOAT = -MAX_FLOAT;

	public static final double MAXPOS_FLOAT = 6.80564694E+38;//2*3.40282347E+38;

	/**
	 * @deprecated Use Double.MIN_VALUE
	 */
	public static final double SMALL_DOUBLE = 1.79769313486231570E-307;

	/**
	 * @deprecated Use Double.MAX_VALUE
	 */
	public static final double MAX_DOUBLE = 1.79769313486231570E+308;

	public static final double MIN_DOUBLE = -MAX_DOUBLE;

	public static final byte BIN_ZERO = 0;

	public static final byte BIN_ONE = -1;

	public static final double SLOPE_MAX = 5000.0;

	public static final double THRESH_INF_SLOPE = 512.0;

	public static final double EPSILON = 1.0E-6;

	public static final double EPSILON3 = 1.0E-3;

	public static final double EPSILON10 = 1.0E-10;

	public static final int sizeOfBYTE = 1; // one byte

	public static final int sizeOfSHORT = 2; // two bytes

	public static final int sizeOfINT = 4; // four bytes

	public static final int sizeOfLONG = 8;

	public static final int sizeOfFLOAT = 4; //four bytes

	public static final int sizeOfDOUBLE = 8;

	/**
	 * @deprecated use Math.toRadians(deg)
	 */
	public static final double Deg2Rad = 3.14159265358979323846 / 180.0;

	/**
	 * @deprecated Use Math.toDegrees(rad)
	 */
	public static final double Rad2Deg = 180.0 / 3.14159265358979323846;

	/**
	 * @deprecated Use Math.PI
	 */
	public static final double PI = 3.14159265358979323846;

	public static final double PrGauss[] = { 0.0, 0.0, 0.1, 0.08, 0.2, 0.158,
			0.3, 0.236, 0.4, 0.311, 0.5, 0.383, 0.6, 0.452, 0.7, 0.516, 0.8,
			0.576, 0.9, 0.632, 1.0, 0.683, 1.1, 0.729, 1.2, 0.77, 1.3, 0.806,
			1.4, 0.838, 1.5, 0.866, 1.6, 0.89, 1.7, 0.911, 1.8, 0.928, 1.9,
			0.943, 2.0, 0.954, 2.1, 0.964, 2.326, 0.98, 2.5, 0.989, 2.576,
			0.99, 3.0, 0.9974, 3.09, 0.998, 3.291, 0.999, 3.5, 0.9995, 4.0,
			0.99994 };

	public static final int NumPrGauss = 30;

	public static double findGaussPr(double prob) {
		// Pr(|u|<=z) = prob, return z
		double z = 0.0F;
		int idx, idx1 = 1;
		for (idx = 3; idx < (NumPrGauss << 1); idx += 2) {			
			if (prob >= PrGauss[idx - 2] && prob <= PrGauss[idx]) {
				idx1 = idx;
				idx = (NumPrGauss << 1);
			}
		}
		if (idx1 == 1)
			return (0.0);

		z = (prob - PrGauss[idx1 - 2]) / (PrGauss[idx1] - PrGauss[idx1 - 2]);
		z = PrGauss[idx1 - 3] + z * (PrGauss[idx1 - 1] - PrGauss[idx1 - 3]);

		return z;
	}

	public int findDigitNumber(int val) {
		int numDigits = 0;
		int num;
		if (val < 0)
			num = -val;
		else
			num = val;

		int[] arr = { 10, 100, 1000, 10000, 100000, 1000000, 10000000,
				100000000 };
		boolean signal = true;
		int exp = 0;
		while (signal && exp < 8) {
			if (num < arr[exp]) {
				numDigits = exp + 1;
				signal = false;
			}
			exp++;
		}
		if (signal)
			numDigits = exp + 1;

		return numDigits;
	}
	// end of class
}
