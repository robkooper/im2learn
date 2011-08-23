package edu.illinois.ncsa.isda.imagetools.ext.texture;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageException;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.ext.conversion.SpFilters;

/**
 * <B> The class IsTexture provides a tool for calculating local image extrema
 * densities, and finding texture regions whose extrema densities are within a
 * given density range. </B> <BR>
 * This tool is based on the paper: <BR>
 * Karu Kalle, Jain Anil K., Bolle Ruud M. "Is there any texture in the image?",
 * Pattern Recognition, Vol. 29, No. 9, pp. 1437 1446, 1996. <BR>
 * <BR>
 * <p>
 * <B>Setup:</B> It is assumed that an image file has been loaded in the main
 * window and an IsTexture dialog has been invoked from the ImLabels menu of the
 * main window. <BR>
 * <BR>
 * <img src="../../../../../../images/imIsTextureDialog.jpg" width="400"
 * height="300">
 * 
 * <BR>
 * IsTexture dialog.
 * 
 * <BR>
 * 
 * <img src="../../../../../../images/imIsTexture1.jpg" width="640"
 * height="480">
 * 
 * <BR>
 * An example original image of sandpaper from CURET database.
 * 
 * <BR>
 * <BR>
 * <img src="../../../../../../images/imIsTexture2.jpg" width="640"
 * height="480"> <BR>
 * Extrema densities image for the sandpaper above, with lighter colors
 * representing higher density.
 * 
 * <BR>
 * <BR>
 * <img src="../../../../../../images/imIsTexture3.jpg" width="640"
 * height="480"> <BR>
 * Binary mask of the density image, with white pixels being within threshold
 * range and black being outside.
 * 
 * <BR>
 * <BR>
 * <img src="../../../../../../images/imIsTexture4.jpg" width="640"
 * height="480"> <BR>
 * Original image overlayed with binary mask.
 * 
 * <BR>
 * <BR>
 * <p>
 * <B>USER INSTRUCTIONS</B> <BR>
 * <B>1)</B> Specify the filter kernel size in pixels by entering row and
 * column kernel dimensions in the edit boxes labeled as Kernel-Row and
 * Kernel-Col. These values of the kernel will used for finding extrema density.
 * <BR>
 * <B>2)</B> Specify the upper and lower threshold values of the local extrema
 * densities in order to characterize texture or non-texture pixels using
 * extrema densities. Pixels with densities within the threshold bounds will be
 * labeled as texture pixels (bright color label), and all other pixels will be
 * labeled as non-texture pixels (dark color label). The threshold bounds must
 * be decimal values between 0 and 1. <BR>
 * <B>3)</B> Move the "Texture Bands" slider to one of the three possible
 * positions (See SLIDER section below). The value of the slider bar is used for
 * assigning the final label in the case of multi-band images since extreme
 * densities are computed per band and have to be combined. <BR>
 * <B>4)</B> Press the "AssignLabels" button to compute results. <BR>
 * <B>5)</B> Optional: Select which resulting images to display using the three
 * checkboxes (See CHECKBOXES section below), then press the "Show" button to
 * display the chosen results. <BR>
 * <B>6)</B> Optional: Press the "Save" button to save resulting images to
 * individual files. <BR>
 * 
 * <BR>
 * <B>TEXT FIELDS</B> <BR>
 * <B>Kernel-Row:</B> Enter the vertical dimension of a filter kernel. It must
 * be a positive integer. <BR>
 * <B>Kernel-Col:</B> Enter the horizontal dimension of a filter kernel. It
 * must be a positive integer. <BR>
 * <B>Lower Threshold:</B> Enter the lower threshold for the calculated extrema
 * densities. Lower threshold must be less than or equal to upper threshold and
 * within the range of [0,1]. When a pixel sample has extrema density between
 * lower and upper thresholds inclusive, then it is a labeled as a texture
 * pixel, otherwise it is labeled as a non-texture pixel. <BR>
 * <B>Upper Threshold:</B> Enter the upper threshold for the calculated extrema
 * densities. Upper threshold must be greater than or equal to lower threshold.
 * It must be a decimal value between 0 and 1 inclusive. When a pixel sample has
 * extrema density between lower and upper thresholds inclusive, then it is a
 * texture pixel sample, otherwise it is a non-texture pixel sample. <BR>
 * 
 * <BR>
 * <B>CHECKBOXES</B> <BR>
 * <B>Extrema Density:</B> Image of extrema densities. The extrema density of a
 * pixel represents the number of extrema per kernel size, found within the
 * kernel centered at this pixel. Extrema density values range between 0 (no
 * extrema) and 1 (all extrema). Extrema are computed independently for each
 * band, therefore the extrema density image has the same number of bands as the
 * input image. <BR>
 * <B>Texture Mask:</B> Binary single-band image where each pixel is 1 if its
 * corresponding pixel in the input image is texture, and 0 if it is
 * non-texture. <BR>
 * <B>Segmented Image:</B> Original image overlayed with texture mask.
 * Non-texture pixels are blacked out, and texture pixels are same as in input
 * image. <BR>
 * 
 * <BR>
 * <B>SLIDER</B> <BR>
 * <B>Texture Bands:</B> This slider is used in multi-band images, because each
 * band is scanned for extrema density independently of the other bands. The
 * position of the slider affects how a pixel is classified to be a texture
 * pixel. This depends on the amount of a pixel's samples classified to be
 * texture. <BR>
 * <B>One:</B> If at least one sample in a pixel is a texture sample then the
 * whole pixel is a texture pixel. <BR>
 * <B>Most:</B> If at least half of the samples in a pixel are texture samples
 * then the whole pixel is a texture pixel. <BR>
 * <B>All:</B> If all samples in a pixel are texture samples then the whole
 * pixel is a texture pixel. <BR>
 * 
 * <BR>
 * <B>BUTTONS</B> <BR>
 * <B>AssignLabels:</B> Performs the required calculations on the input image
 * and creates 3 images from it, but does not show them. <BR>
 * <B>Show:</B> Shows the images the images created with "AssignLabels" and
 * selected in the 3 checkboxes above this button. <BR>
 * <B>Save:</B> Opens a save file dialog box for each of the 3 images created
 * with the "AssignLabels" or "Show" buttons. You can then save each image to a
 * different file, or press cancel to not save a given image. <BR>
 * <B>Done:</B> Closes the IsTexture dialog box, as well as all images opened
 * with the "Show" button. <BR>
 * 
 * <BR>
 * <B>NOTE</B> <BR>
 * Pressing "AssignLabels" will not display the images but they are computed and
 * can be saved with "Save". <BR>
 * Always press the "AssignLabels" button after loading a new image. Pressing
 * "Show" without "AssignLabels" will give results based on old input image.
 * <BR>
 * If the input image is unchanged, and only the parameters in the textboxes, or
 * the slider position have changed then pressing "AssignLabels" is not
 * necessary. <BR>
 * Make sure to press "AssignLabels" or "Show" before using "Save". <BR>
 * </p>
 * 
 * </p>
 * Release notes: </B> <BR>
 * 
 * @author Martin Urban
 * @version 1.0
 * 
 */

public class IsTexture {
    private int    _kernelHeight            = 0;
    private int    _kernelWidth             = 0;
    private double _lowerThreshold          = 0;
    private double _upperThreshold          = 1;
    private int    _textureBandCountMinimum = 0;

    public IsTexture() {
    }

    public IsTexture(int kernelHeight, int kernelWidth, double thresholdLow, double thresholdUp) {
        if (kernelHeight >= 1)
            _kernelHeight = kernelHeight;
        else
            _kernelHeight = 1;

        if (kernelWidth >= 1)
            _kernelWidth = kernelWidth;
        else
            _kernelWidth = 1;

        if (thresholdLow >= 0 && thresholdLow <= 1)
            _lowerThreshold = thresholdLow;
        else
            _lowerThreshold = 0;

        if (thresholdUp >= 0 && thresholdUp <= 1)
            _upperThreshold = thresholdUp;
        else
            _upperThreshold = 1;

        if (_lowerThreshold > _upperThreshold) {
            System.out.println("ERROR: low is greater than high, setting high equal to low");
            _upperThreshold = _lowerThreshold;
        }
    }

    // setters and getters
    public void setKernelHeight(int height) {
        if (height >= 1)
            _kernelHeight = height;
        else
            _kernelHeight = 1;
    }

    public int getKernelHeight() {
        return _kernelHeight;
    }

    public void setKernelWidth(int width) {
        if (width >= 1)
            _kernelWidth = width;
        else
            _kernelWidth = 1;
    }

    public int getKernelWidth() {
        return _kernelWidth;
    }

    public void setLowerThreshold(double min) {
        if (min < 0)
            _lowerThreshold = 0;
        else if (min > 1)
            _lowerThreshold = 0;
        else if (min > _upperThreshold)
            _lowerThreshold = _upperThreshold;
        else
            _lowerThreshold = min;
    }

    public double getLowerThreshold() {
        return _lowerThreshold;
    }

    public void setUpperThreshold(double max) {
        if (max < 0)
            _upperThreshold = 1;
        else if (max > 1)
            _upperThreshold = 1;
        else if (max < _lowerThreshold)
            _upperThreshold = _lowerThreshold;
        else
            _upperThreshold = max;
    }

    public double getUpperThreshold() {
        return _upperThreshold;
    }

    public void setTextureBandCountMin(int bands) {
        if (bands < 0)
            _textureBandCountMinimum = 0;
        else
            _textureBandCountMinimum = bands;
    }

    public int getTextureBandCountMin() {
        return _textureBandCountMinimum;
    }

    /**
     * Scans all rows and columns of input image for local minima and maxima
     * returned image is same size as input image
     * 
     * @param inputImage
     *            Image to scan (BYTE or DOUBLE type)
     * @return DOUBLE type ImageObject with tags for each pixel in input image
     *         showing if it is a local row or column min or max
     */
    public ImageObject scanImage(ImageObject inputImage) throws ImageException {
        // sanity check
        if (inputImage == null) {
            throw (new ImageException("No input data specified."));
        }

        boolean check;
        ImageObject extremaMarks = ImageObject.createImage(inputImage.getNumRows(), inputImage.getNumCols(), inputImage.getNumBands(), inputImage.getType());
        // scan rows for local row minima, maxima
        int bandsToScan = inputImage.getNumBands();
        // bandsToScan = 1;

        for (int band = 0; band < bandsToScan; band++)
            for (int i = 0; i < extremaMarks.getNumRows(); i++)
                check = scanRow(i, band, inputImage, extremaMarks);

        // scan columns for local column minima, maxima
        for (int band = 0; band < bandsToScan; band++)
            for (int i = 0; i < extremaMarks.getNumCols(); i++)
                check = scanColumn(i, band, inputImage, extremaMarks);

        return extremaMarks;
    }

    /**
     * Marks pixels in this column as local max or min
     * 
     * @param colIndex
     *            Column number to scan
     * @param band
     *            Band number to scan
     * @param data
     *            Image data
     * @param marks
     *            DOUBLE type image object with minimum maximum markings in
     *            pixels (this image is changed on return)
     * @return True on success, false on failure
     */
    protected boolean scanColumn(int colIndex, int band, ImageObject data, ImageObject marks) {
        if (data == null || marks == null) {
            System.err.println("ERROR: missing input data or mask data");
            return false;
        }

        int w = data.getNumCols();
        int h = data.getNumRows();
        int numBands = data.getNumBands();
        if ((colIndex < 0) || (colIndex >= w)) {
            System.err.print("ERROR: Column index is out of bounds");
            return false;
        }

        int index;
        int maxIndex;
        int maxMark;
        int minMark;
        int a = 0;
        int inc; // increment amount
        int slope = 0; // 0 for steady, 1 for rising, -1 for falling

        index = colIndex * numBands + band;
        maxIndex = (colIndex + (h - 1) * w - 1) * numBands + band;
        minMark = 8;
        maxMark = 12;
        inc = w * numBands;

        // if height is 1, nothing to do
        if (h == 1)
            return true;

        double pixel;
        double pixelPrev;
        double pixelNext;
        // first pixel in col
        pixel = data.getDouble(index);
        pixelNext = data.getDouble(index + inc);
        if (pixel > pixelNext)
            marks.set(index, marks.getDouble(index) + maxMark);
        else if (pixel < pixelNext)
            marks.set(index, marks.getDouble(index) + minMark);
        else if (pixel == pixelNext)
            a = index;

        // middle (non endpoint) pixels
        index = index + inc;
        while (index < maxIndex) {
            pixelPrev = pixel;
            pixel = pixelNext;
            pixelNext = data.getDouble(index + inc);

            // see if pixel is local max, min, or constant
            // current pixel is bigger than previous pixel
            if (pixel > pixelPrev) {
                if (pixel > pixelNext)
                    marks.set(index, marks.getDouble(index) + maxMark);
                else if (pixel == pixelNext) {
                    slope = 1;
                    a = index;
                }
            }
            // current pixel is smaller than previous pixel
            else if (pixel < pixelPrev) {
                if (pixel < pixelNext)
                    marks.set(index, marks.getDouble(index) + minMark);
                else if (pixel == pixelNext) {
                    slope = -1;
                    a = index;
                }
            }
            // current pixel is equal to previous pixel
            else {
                int ind = (w * (((a / w + index / w) / numBands) / 2) + colIndex) * numBands + band;
                if ((pixel > pixelNext) && (slope >= 0))
                    marks.set(ind, marks.getDouble(ind) + maxMark);
                else if ((pixel < pixelNext) && (slope <= 0))
                    marks.set(ind, marks.getDouble(ind) + minMark);
            }
            index = index + inc;
        }

        // last pixel in col
        pixelPrev = pixel;
        pixel = pixelNext;
        if (pixel > pixelPrev)
            marks.set(index, marks.getDouble(index) + maxMark);
        else if (pixel < pixelPrev)
            marks.set(index, marks.getDouble(index) + minMark);
        else {
            int ind = (w * (((a / w + index / w) / numBands) / 2) + colIndex) * numBands + band;
            if (slope > 0)
                marks.set(ind, marks.getDouble(ind) + maxMark);
            else if (slope < 0)
                marks.set(ind, marks.getDouble(ind) + minMark);
        }

        return true;
    }

    /**
     * Marks pixels in this row as local max or min
     * 
     * @param rowIndex
     *            Row number to scan
     * @param band
     *            Band number to scan
     * @param data
     *            Image data
     * @param marks
     *            DOUBLE type image object with minimum maximum markings in
     *            pixels (this image is changed on return)
     * @return True on success, false on failure
     */
    protected boolean scanRow(int rowIndex, int band, ImageObject data, ImageObject marks) {
        if (data == null || marks == null) {
            System.err.println("ERROR: missing input data or mask data");
            return false;
        }

        int w = data.getNumCols();
        int h = data.getNumRows();
        int numBands = data.getNumBands();

        if ((rowIndex < 0) || (rowIndex >= h)) {
            System.err.print("ERROR: Row index is out of bounds");
            return false;
        }

        int index;
        int maxIndex;
        int maxMark;
        int minMark;
        int a = 0;
        int inc; // increment amount
        int slope = 0; // 0 for steady, 1 for rising, -1 for falling

        index = rowIndex * w * numBands + band;
        maxIndex = (rowIndex * w + w - 1) * numBands + band;
        minMark = 2;
        maxMark = 3;
        inc = numBands;

        // if width is 1, nothing to do
        if (w == 1)
            return true;

        double pixel;
        double pixelPrev;
        double pixelNext;
        // first pixel in row
        pixel = data.getDouble(index);
        pixelNext = data.getDouble(index + inc);
        if (pixel > pixelNext)
            marks.set(index, marks.getDouble(index) + maxMark);
        else if (pixel < pixelNext)
            marks.set(index, marks.getDouble(index) + minMark);
        else if (pixel == pixelNext)
            a = index;

        // middle (non endpoint) pixels in row
        index = index + inc;
        while (index < maxIndex) {
            pixelPrev = pixel;
            pixel = pixelNext;
            pixelNext = data.getDouble(index + inc);

            // see if pixel is local max, min, or constant
            // current pixel is bigger than previous pixel
            if (pixel > pixelPrev) {
                if (pixel > pixelNext)
                    marks.set(index, marks.getDouble(index) + maxMark);
                else if (pixel == pixelNext) {
                    slope = 1;
                    a = index;
                }
            }
            // current pixel is smaller than previous pixel
            else if (pixel < pixelPrev) {
                if (pixel < pixelNext)
                    marks.set(index, marks.getDouble(index) + minMark);
                else if (pixel == pixelNext) {
                    slope = -1;
                    a = index;
                }
            }
            // current pixel is equal to previous pixel
            else {
                int ind = (((a + index) / numBands) / 2) * numBands + band;
                if ((pixel > pixelNext) && (slope >= 0))
                    marks.set(ind, marks.getDouble(ind) + maxMark);
                else if ((pixel < pixelNext) && (slope <= 0))
                    marks.set(ind, marks.getDouble(ind) + minMark);
            }
            index = index + inc;
        }

        // last pixel in row
        pixelPrev = pixel;
        pixel = pixelNext;
        if (pixel > pixelPrev)
            marks.set(index, marks.getDouble(index) + maxMark);
        else if (pixel < pixelPrev)
            marks.set(index, marks.getDouble(index) + minMark);
        else {
            int ind = (((a + index) / numBands) / 2) * numBands + band;
            if (slope > 0)
                marks.set(ind, marks.getDouble(ind) + maxMark);
            else if (slope < 0)
                marks.set(ind, marks.getDouble(ind) + minMark);
        }

        return true;
    }

    /**
     * Scans all bands of image and sets pixel sample values to 1 if value is
     * between lowThreshold and highThreshold inclusive, sets to 0 otherwise
     * 
     * @param inImage
     *            ImageObject
     * @return Binary ImageObject with pixel sample values 1 when the input
     *         pixels are within the threshold, 0 otherwise
     */
    public ImageObject thresholdDensityImage(ImageObject inImage) {
        // sanity check
        if (inImage == null) {
            System.out.println("ERROR: input image is null");
            return null;
        }

        long size = inImage.getSize();

        for (int i = 0; i < size; i++)
            if ((inImage.getDouble(i) >= _lowerThreshold) && (inImage.getDouble(i) <= _upperThreshold))
                inImage.set(i, 1);
            else
                inImage.set(i, 0);

        /*
         * else if (inImage.sampType.equalsIgnoreCase("SHORT")){ for (int i = 0;
         * i < size; i++) if ( (inImage.imageShort[i] >= _lowerThreshold) &&
         * (inImage.imageShort[i] <= _upperThreshold)) inImage.imageShort[i] =
         * 1; else inImage.imageShort[i] = 0; } else if
         * (inImage.sampType.equalsIgnoreCase("INT")){ for (int i = 0; i < size;
         * i++) if ( (inImage.imageInt[i] >= _lowerThreshold) &&
         * (inImage.imageInt[i] <= _upperThreshold)) inImage.imageInt[i] = 1;
         * else inImage.imageInt[i] = 0; } else if
         * (inImage.sampType.equalsIgnoreCase("LONG")){ for (int i = 0; i <
         * size; i++) if ( (inImage.imageLong[i] >= _lowerThreshold) &&
         * (inImage.imageLong[i] <= _upperThreshold)) inImage.imageLong[i] = 1;
         * else inImage.imageLong[i] = 0; } else if
         * (inImage.sampType.equalsIgnoreCase("FLOAT")){ for (int i = 0; i <
         * size; i++) if ( (inImage.imageFloat[i] >= _lowerThreshold) &&
         * (inImage.imageFloat[i] <= _upperThreshold)) inImage.imageFloat[i] =
         * 1; else inImage.imageFloat[i] = 0; } else if
         * (inImage.sampType.equalsIgnoreCase("DOUBLE")){ for (int i = 0; i <
         * size; i++) if ( (inImage.imageDouble[i] >= _lowerThreshold) &&
         * (inImage.imageDouble[i] <= _upperThreshold)) inImage.imageDouble[i] =
         * 1; else inImage.imageDouble[i] = 0; }
         */
        return inImage;
    }

    /**
     * Changes multiple band texture mask into single-band binary mask using
     * _textureBandCountMinimum value
     * 
     * @param inImage
     *            ImageObject Multi-band texture mask of DOUBLE type
     * @return ImageObject Single band BYTE type texture mask
     */
    public ImageObject toOneBandTextureMask(ImageObject inImage) throws ImageException {
        // sanity check
        if (inImage == null) {
            System.out.println("ERROR: input image is null");
            return null;
        }

        int w = inImage.getNumCols();
        int h = inImage.getNumRows();
        int numBands = inImage.getNumBands();
        ImageObject outImage = ImageObject.createImage(h, w, 1, ImageObject.TYPE_BYTE);

        long size = w * h;
        int textureBandsCount;

        for (int i = 0; i < size; i++) {
            textureBandsCount = 0;
            // count how many bands of this pixel are textures
            for (int band = 0; band < numBands; band++)
                if (inImage.getDouble(numBands * i + band) == 1)
                    textureBandsCount++;
            // if at least textureBandMinimum+1 bands in pixel are texture then
            // all bands in pixel are texture
            if (textureBandsCount > _textureBandCountMinimum)
                outImage.set(i, 1);
        }

        return outImage;
    }

    /**
     * Marks with 1 the pixels that are one of the following two types: 1) local
     * row minimum and local column minimum 2) local row maximum and local
     * column maximum All other pixels are marked 0
     * 
     * @param inImage
     *            Input DOUBLE type ImageObject already processed with scanRow
     *            and scanColumn
     */
    public void detectExtrema(ImageObject inImage) {
        // sanity check
        if (inImage == null) {
            System.err.println("ERROR: input image is null");
            return;
        }

        long pixel;
        int valMinMin = 10;
        int valMinMax = 14;
        int valMaxMin = 11;
        int valMaxMax = 15;

        for (int i = 0; i < inImage.getSize(); i++) {
            pixel = 0;
            pixel = (long) inImage.getDouble(i);
            // if row min and col min
            if (pixel == valMinMin)
                inImage.set(i, 1);
            // if row max and col max
            else if (pixel == valMaxMax)
                inImage.set(i, 1);
            // if row min and col max
            else if (pixel == valMinMax)
                inImage.set(i, 1);
            // if row max and col min
            else if (pixel == valMaxMin)
                inImage.set(i, 1);
            else
                inImage.set(i, 0);
        }

        return;
    }

    /**
     * Detects density of extrema in the kernel for each pixel
     * 
     * @param imObject
     *            Input DOBLE type ImageObject
     * @return Image object filtered with kernel to find density of extrema in
     *         kernel area
     */
    public ImageObject LowPassOut(ImageObject imObject) {
        // insert sanity check

        SpFilters spfilter = new SpFilters();
        spfilter.setImage(imObject);
        spfilter.setKernel(_kernelHeight, _kernelWidth);
        if (spfilter.filter(5))
            return spfilter.getResult();
        else
            return null;
    }

    /**
     * Sets all non-texture pixels in inImage to 0, according to the texMarks
     * image
     * 
     * @param inImage
     *            Original image being scanned
     * @param textureMarks
     *            ImageObject of 1's (texture) and 0's (non-texture)
     * @return Input image with non-texture pixels blacked out
     */
    public ImageObject suppressNontexture(ImageObject inImage, ImageObject textureMarks) throws ImageException {
        // sanity check
        if (inImage == null) {
            System.out.println("ERROR: input image is null");
            return null;
        }
        if (textureMarks == null) {
            System.out.println("ERROR: textureMarks image is null");
            return null;
        }

        int w = inImage.getNumCols();
        int h = inImage.getNumRows();
        int numBands = inImage.getNumBands();

        ImageObject outImage = ImageObject.createImage(h, w, numBands, inImage.getType());

        long size = w * h;

        for (int i = 0; i < size; i++) {
            // if at least _textureBandMinimum+1 bands in pixel are texture then
            // all bands in pixel are texture
            if (textureMarks.getDouble(i) == 1)
                for (int band = 0; band < numBands; band++)
                    outImage.set(numBands * i + band, inImage.getDouble(numBands * i + band));
            // set all bands of this pixel to 0
            else
                for (int band = 0; band < numBands; band++)
                    outImage.set(numBands * i + band, 0);
        }

        /*
         * if (inImage.sampType.equalsIgnoreCase("SHORT")){ for (int i = 0; i <
         * size; i++) { // if at least _textureBandMinimum+1 bands in pixel are
         * texture then all bands in pixel are texture if (textureMarks.image[i] ==
         * 1) for (int band = 0; band < numBands; band++)
         * outImage.imageShort[numBands * i + band] =
         * inImage.imageShort[numBands * i + band]; // set all bands of this
         * pixel to 0 else for (int band = 0; band < numBands; band++)
         * outImage.imageShort[numBands * i + band] = 0; } } if
         * (inImage.sampType.equalsIgnoreCase("INT")){ for (int i = 0; i < size;
         * i++) { // if at least _textureBandMinimum+1 bands in pixel are
         * texture then all bands in pixel are texture if (textureMarks.image[i] ==
         * 1) for (int band = 0; band < numBands; band++)
         * outImage.imageInt[numBands * i + band] = inImage.imageInt[numBands *
         * i + band]; // set all bands of this pixel to 0 else for (int band =
         * 0; band < numBands; band++) outImage.imageInt[numBands * i + band] =
         * 0; } } if (inImage.sampType.equalsIgnoreCase("LONG")){ for (int i =
         * 0; i < size; i++) { // if at least _textureBandMinimum+1 bands in
         * pixel are texture then all bands in pixel are texture if
         * (textureMarks.image[i] == 1) for (int band = 0; band < numBands;
         * band++) outImage.imageLong[numBands * i + band] =
         * inImage.imageLong[numBands * i + band]; // set all bands of this
         * pixel to 0 else for (int band = 0; band < numBands; band++)
         * outImage.imageLong[numBands * i + band] = 0; } } if
         * (inImage.sampType.equalsIgnoreCase("FLOAT")){ for (int i = 0; i <
         * size; i++) { // if at least _textureBandMinimum+1 bands in pixel are
         * texture then all bands in pixel are texture if (textureMarks.image[i] ==
         * 1) for (int band = 0; band < numBands; band++)
         * outImage.imageFloat[numBands * i + band] =
         * inImage.imageFloat[numBands * i + band]; // set all bands of this
         * pixel to 0 else for (int band = 0; band < numBands; band++)
         * outImage.imageFloat[numBands * i + band] = 0; } } if
         * (inImage.sampType.equalsIgnoreCase("DOUBLE")){ for (int i = 0; i <
         * size; i++) { // if at least _textureBandMinimum+1 bands in pixel are
         * texture then all bands in pixel are texture if (textureMarks.image[i] ==
         * 1) for (int band = 0; band < numBands; band++)
         * outImage.imageDouble[numBands * i + band] =
         * inImage.imageDouble[numBands * i + band]; // set all bands of this
         * pixel to 0 else for (int band = 0; band < numBands; band++)
         * outImage.imageDouble[numBands * i + band] = 0; } }
         */return outImage;
    }
}
