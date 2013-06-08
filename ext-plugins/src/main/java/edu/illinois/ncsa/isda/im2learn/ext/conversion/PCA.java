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
package edu.illinois.ncsa.isda.im2learn.ext.conversion;


import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageException;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObjectDouble;

import java.text.DecimalFormat;

/**
 * <B>The class ImagePCA provides a tool for performing principal component
 * analysis (PCA) of images. </B> This functionality is available from the
 * ncsa.im2learn.main menu in Im2Learn under features, and will be applied immediately
 * to the current image in the ncsa.im2learn.main Im2Learn frame. If the current image
 * was obtained by forward PCA then it will be converted back to the orifinal
 * image without any loss of information.
 * <p/>
 * <BR> <BR> <B>Description:</B> The forward PCA of an image is calculated by
 * first creating a correlation matrix of the image. This matrix will show how
 * each band is related to each other band. The PCA code will use the
 * correlation to compute a new transformation based on eigenvectors. This new
 * foward transformation matrix will convert the original image into a PCA
 * image. Each of the bands in the new PCA image will contain information from
 * all the bands of the original image. The first band in the PCA image has the
 * largest contribution from the original image and each additional band in the
 * PCA image has smaller contribution from the original image.
 * <p/>
 * To recover the original image from the PCA image, coefficients of the
 * transformation matrix ("eigenvectors") are stored in image properties, as
 * well as, the type of the original image ("originaltype"). These image
 * properties are used when reverse PCA is being executed.
 * <p/>
 * An example of a PCA image transformation is shown below for the "Runner"
 * image.
 * <p/>
 * <img src="doc-files/PCA-1.jpg">
 * <p/>
 * <img src="doc-files/PCA-2.jpg">
 * <p/>
 * This class also provides utility functions to calculate a covariance of an
 * image, calcCovarianceMatrix, as well as, the correlation,
 * calcCorrelationMatrix.
 * <p/>
 *
 * @author Peter Groves
 * @author Rob Kooper
 * @version 2.0
 */
public class PCA {

    public static String EIGENVECTOR = "eigenvectors";
    public static String ORIGINALTYPE = "originaltype";

    private static Log logger = LogFactory.getLog(PCA.class);

    /**
     * number of components to use to generate PCA image
     */
    private int components = -1;

    /**
     * the image created by transforming the input image
     */
    ImageObject compIm;

    /**
     * original type of image
     */
    String origtype = null;

    /**
     * correlation matrix
     */
    private double[][] covMat;

    private String[] covLabels = null;

    protected double[] eigenvalue = null;
    protected double[][] eigenvector = null;

    protected boolean verbose = true;


    /**
     * Calculate the PCA of the matrix. What this will do is calculate the
     * eigenvalues and eigenvectors of the matrix. It will keep them in the
     * eigenvalues array (per band) and the eigenvector array (with the vector
     * in each column for each band. The eigenvector array will be [band][pca],
     * so to calculate the PCA image from this you will need to multiply such
     * that img[r, c, pca] = sum(b=1..max of vector[pca][b]*img[r, c, b]);
     *
     * @param matrix
     */
    public void calculate(double[][] matrix) {
        int len = matrix.length;

        Matrix jmatrix = new Matrix(matrix);
        EigenvalueDecomposition eig = jmatrix.eig();
        eigenvector = new double[len][len];
        for (int r = 0; r < len; r++) {
            for (int c = 0; c < len; c++) {
                eigenvector[c][r] = eig.getV().get(r, len - c - 1);
            }
        }

        double[][] tmp = eig.getD().getArray();
        eigenvalue = new double[len];
        for (int i = 0, j = len - 1; i < len; i++, j--) {
            eigenvalue[j] = tmp[i][i];
        }
    }

    public void printPCA() {
        int len = eigenvalue.length;

        double x = 0;
        for (int c = 0; c < len; c++) {
            x += getValue()[c];
            printDouble(getValue()[c], "######0.000  ");
        }
        System.out.println();

        for (int c = 0; c < len; c++) {
            printDouble(getValue()[c] / x, "######0.000% ");
        }
        System.out.println();

        for (int b = 0; b < len; b++) {
            for (int pc = 0; pc < len; pc++) {
                printDouble(getVector()[pc][b], "######0.000  ");
            }
            System.out.println();
        }
    }

    private void printDouble(double d, String format) {
        DecimalFormat df = new DecimalFormat(format);
        String s = df.format(d);
        for (int i = s.length(); i < format.length(); i++) {
            System.out.print(" ");
        }
        System.out.print(s);
    }

    public double[][] getVector() {
        return eigenvector;
    }

    public double[] getValue() {
        return eigenvalue;
    }

    /**
     * this does all the work. after setting all the necessary parameters, call
     * this, and then the loadings and eigenvalues can be accessed from the
     * getter methods
     */
    public boolean computeLoadings(ImageObject im) {
        //covMat = calcCovarianceMatrix(im);
        //covMat = calcCovariance(im);  // unstandardized PCA
        covMat = calcCorrelationMatrix(im);  // standardized PCA

        covLabels = new String[im.getNumBands()];
        for (int i = 0; i < covLabels.length; i++) {
            covLabels[i] = "Band " + i;
        }

        calculate(covMat);
        printPCA();

        return true;
    }

    public void showCorrelationMatrix() {
        //new CorrelationMatrix(covMat, covLabels);
    }

    public void showPCAMatrix() {
        //new CorrelationMatrix(eigenvector, covLabels);
    }

    public void setComponents(int components) {
        this.components = components;
    }

    public int getComponents() {
        return components;
    }

    /**
     * Applies the Principal Component transformation to an input image.
     * <code>computeLoadings</code> must be called prior to this message. Also,
     * the number of bands in the new image will be the number of components
     * specified before the last call to computeLoadings. Note that the image
     * used here may be different than the one passed into
     * <code>computeLoadings</code>, but they must have the same number of
     * bands.
     * <p/>
     * The resulting image can be accessed by <code>getPCAImage()</code>.
     */
    public ImageObject applyPCATransform(ImageObject im) {
        if (im == null)
            return null;

        int numRows = im.getNumRows();
        int numCols = im.getNumCols();
        int numBands = im.getNumBands();
        int comp = components;
        if ((comp < 1) || (comp > numBands)) {
            comp = eigenvector.length;
        }

        origtype = ImageObject.types[im.getType()];
        compIm = new ImageObjectDouble(numRows, numCols, comp);
        compIm.setProperty(EIGENVECTOR, eigenvector);
        compIm.setProperty(ORIGINALTYPE, origtype);

        int i, pca, r, c, b;
        double v;

        // img[r, c, pca] = sum(b=1..max of vector[pca][b]*img[r, c, b]);
        for (pca = 0; pca < comp; pca++) {
            i = pca;
            for (r = 0; r < numRows; r++) {
                for (c = 0; c < numCols; c++, i += comp) {
                    v = 0;
                    for (b = 0; b < numBands; b++) {
                        v += eigenvector[pca][b] * im.getDouble(r, c, b);
                    }
                    compIm.set(i, v);
                }
            }
        }
        return compIm;
    }

    public ImageObject undoPCATransform(ImageObject im) {
        if (im == null) {
            return null;
        }

        if ((eigenvector == null) && (im.getProperty(EIGENVECTOR) != null)) {
            eigenvector = (double[][]) im.getProperty(EIGENVECTOR);
        }
        if ((origtype == null) && (im.getProperty(ORIGINALTYPE) != null)) {
            origtype = (String) im.getProperty(ORIGINALTYPE);
        }

        int numRows = im.getNumRows();
        int numCols = im.getNumCols();
        int numComps = eigenvector.length;
        int numBands = eigenvector[0].length;

        try {
            compIm = ImageObject.createImage(numRows, numCols, numComps, origtype);
        } catch (ImageException exc) {
            logger.error("Error creating image", exc);
            return null;
        }

        int r, c, b, pca;
        double v;
        int comp = components;
        if ((comp < 1) || (comp > im.getNumBands())) {
            comp = im.getNumBands();
        }

        for (b = 0; b < numBands; b++) {
            for (r = 0; r < numRows; r++) {
                for (c = 0; c < numCols; c++) {
                    v = 0;
                    for (pca = 0; pca < comp; pca++) {
                        v += eigenvector[pca][b] * im.getDouble(r, c, pca);
                    }
                    compIm.set(r, c, b, v);
                }
            }
        }
        return compIm;
    }

    /**
     * The image that is produced by <code>applyPCATransform</code> and the
     * <code>undoPCATransform</code>
     */
    public ImageObject getResult() {
        return compIm;
    }

    ///////////////////////////////////////////////////
    //*	Covariance Matrix
    //////////////////////////////////////////////////

    public static double[][] calcCovariance(ImageObject imgobj) {
        int bands = imgobj.getNumBands();
        long pixels = imgobj.getNumCols() * imgobj.getNumRows();
        double[][] result = new double[bands][bands];
        double[] mean = new double[bands];

        // first find mean of each band
        for (int i = 0; i < imgobj.getSize(); i++) {
            mean[i % bands] += imgobj.getDouble(i);
        }
        for (int i = 0; i < bands; i++) {
            mean[i] /= pixels;
            logger.info("mean band " + i + " = " + mean[i]);
        }

        // calculate covariance matrix
        for (int i = 0; i < bands; i++) {
            for (int j = i; j < bands; j++) {
                for (int p = 0, pi = i, pj = j; p < pixels; p++, pi += bands, pj += bands) {
                    double xi = imgobj.getDouble(pi);
                    double xj = imgobj.getDouble(pj);
                    result[i][j] += (xi - mean[i]) * (xj - mean[j]);
                }
            }
        }

        for (int i = 0; i < bands; i++) {
            for (int j = i; j < bands; j++) {
                result[i][j] /= (pixels - 1);
                result[j][i] = result[i][j];
            }
        }

        return result;
    }

    public static double[][] calcCorrelationMatrix(ImageObject imgobj) {
        double[][] result = calcCovariance(imgobj);

        for (int i = 0; i < result.length; i++) {
            result[i][i] = Math.sqrt(result[i][i]);
            if (result[i][i] == 0.0) result[i][i] = 1.0;
        }

        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result[i].length; j++) {
                if (i != j) {
                    result[i][j] = result[i][j] / (result[i][i] * result[j][j]);
                }
            }
        }

        for (int i = 0; i < result.length; i++) {
            result[i][i] = 1.0;
        }

        return result;
    }

    /**
     * calculates the covariance matrix of the bands over all examples
     *
     * @param im the input ImageObject
     * @return cov the covariance matrix in a double[][]
     */
    public static double[][] calcCovarianceMatrix(ImageObject im) {
        int i, j;
        double d;

        int numBands = im.getNumBands();

        double[][] covs = new double[numBands][numBands];

        //calculate the means
        double[] means = new double[numBands];

        int index = 0;
        int baseIndex = 0;
        double b;
        int numPix = im.getNumRows() * im.getNumCols();

        for (int p = 0; p < numPix; p++) {
            for (i = 0; i < numBands; i++) {

                means[i] += im.getDouble(baseIndex);//im.image[baseIndex];
                b = im.getDouble(baseIndex);//im.image[baseIndex];
                index = baseIndex;


                for (j = i; j < numBands; j++) {
                    d = b * im.getDouble(index);//im.image[index];
                    covs[i][j] += d;
                    index++;
                }

                baseIndex++;
            }
        }
        for (i = 0; i < numBands; i++) {
            means[i] /= numPix;

            for (j = 0; j < i; j++) {
                covs[i][j] /= numPix;
                covs[i][j] -= (means[i] * means[j]);
                //make the covariance matrix symmetrical
                covs[j][i] = covs[i][j];
            }
        }
        return covs;
    }
}

