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
package edu.illinois.ncsa.isda.imagetools.ext.math;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Title: Matrix
 * <p/>
 * Description: This is an interface to access eigen vector computation in the
 * JAMA
 * <p/>
 *
 * @author Sang-Chul Lee
 * @version 1.0
 */

public class MatrixExt {
    static private Log logger = LogFactory.getLog(MatrixExt.class);

    //  static double[][] array = {{1.,2.,3},{4.,5.,6.},{7,8,9}}; // column major matrix
    private Matrix mat;
    private int row, col;
    private EigenvalueDecomposition e;

    public MatrixExt(int row, int col, double[] array) {
        setMatrix(row, col, array);
    }

    public MatrixExt(int row, int col, double initial) {
        setMatrix(row, col, initial);
    }

    public MatrixExt(Matrix m) {
        mat = m;
        row = m.getRowDimension();
        col = m.getColumnDimension();
    }

    public void setMatrix(int row, int col, double[] array) throws IllegalArgumentException {
        if (row * col == array.length) {
            int index;
            mat = new Matrix(row, col);
            this.row = row;
            this.col = col;

            index = 0;
            for (int j = 0; j < row; j++) {
                for (int i = 0; i < col; i++) {
                    mat.set(j, i, array[index++]);
                }
            }
        } else {
            throw(new IllegalArgumentException("Error on array dimension"));
        }
    }

    public void setMatrix(int row, int col, double initial) {
        mat = new Matrix(row, col);
        this.row = row;
        this.col = col;
        for (int j = 0; j < row; j++) {
            for (int i = 0; i < col; i++) {
                mat.set(j, i, initial);
            }
        }
    }

    public double[][] getArray() {
        return (mat.getArray());
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public boolean calculateEigen() throws IllegalArgumentException {
        if (row != col) {
            throw(new IllegalArgumentException("Error on array dimension"));
        } else {
            e = new EigenvalueDecomposition(mat);
            return true;
        }
    }

    public MatrixExt getEigenValue() {
        MatrixExt ret = new MatrixExt(e.getD());
        return ret;
    }

    public MatrixExt getEigenVector() {
        MatrixExt ret = new MatrixExt(e.getV());
        return ret;
    }


    public double getSmallestEigenValue() {
        int i = getSmallestIndex();
        return (e.getD().get(i, i));
    }

    public MatrixExt getSmallestValuedVector() {
        double[] temp = new double[row];
        for (int i = 0; i < row; i++) {
            temp[i] = e.getV().get(i, getSmallestIndex());
        }
        return (new MatrixExt(row, 1, temp));
    }

    public double[] getSmallestValuedVectorArray() {
        double[] temp = new double[row];
        for (int i = 0; i < row; i++) {
            temp[i] = e.getV().get(i, getSmallestIndex());
        }
        return (temp);
    }

    private int getSmallestIndex() {
        double val = Double.MAX_VALUE;
        int index = -1;
        Matrix temp = e.getD();

        for (int i = 0; i < temp.getColumnDimension(); i++) {
            if (Math.abs(temp.get(i, i)) < val) {
                val = Math.abs(temp.get(i, i));
                index = i;
            }
        }
        return index;
    }

    public MatrixExt plus(MatrixExt b) {
        return (new MatrixExt(mat.plus(b.mat)));
    }

    public MatrixExt minus(MatrixExt b) {
        return (new MatrixExt(mat.minus(b.mat)));
    }

    public MatrixExt multiply(MatrixExt b) {
        return (new MatrixExt(mat.times(b.mat)));
    }

    public MatrixExt inverse() {
        return (new MatrixExt(mat.inverse()));
    }

    public MatrixExt transpose() {
        return (new MatrixExt(mat.transpose()));
    }

    /*  public static void ncsa.im2learn.main(String args[]){
         = new Jama.Matrix(array);

        printMatrix(A);
        EigenvalueDecomposition eigenValue = new EigenvalueDecomposition(A);

        printMatrix(eigenValue.getD());
        printMatrix(eigenValue.getV());

      }
    */
    public void printMatrix() {
        double[][] arr;
        arr = mat.getArrayCopy();
        logger.debug(mat.getColumnDimension() + " " + mat.getRowDimension());

        for (int j = 0; j < mat.getRowDimension(); j++) {
            String s = "";
            for (int i = 0; i < mat.getColumnDimension(); i++) {
                s += arr[j][i] + " ";
            }
            logger.debug(s);
        }
    }
}
