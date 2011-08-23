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
package edu.illinois.ncsa.isda.imagetools.ext.statistics;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObjectInt;
import edu.illinois.ncsa.isda.imagetools.core.display.*;
import edu.illinois.ncsa.isda.imagetools.core.io.FileChooser;
import edu.illinois.ncsa.isda.imagetools.core.io.ImageLoader;
import edu.illinois.ncsa.isda.imagetools.ext.misc.ImageFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;

// TODO fix class to be good java (ie functions lowercase etc).

/**
 * <b>Description:</b> The class ImageCompareDialog provides a graphics user
 * interface (GUI) to the class ImageCompare designed for comparing two images
 * for similarity.
 * <p/>
 * Even though to the human eye two images look the same there can be some
 * minute differences, this dialog will allow the user to select a comparison
 * method to compare two images and visualize the minute differences between the
 * images. The tool will allow the user to load a test image, a mask (or use a
 * predefined mask) and test method, as well as a multiplier for the result
 * image and the type of the result image. <br> <img src="../../../../../../images/ImageCompare.jpg"><br>
 * <br> The dialog shows all these options. To perform a comparison the user
 * must first select an image to compare the current image in the mainfram
 * against using the "Load Test" button. This will show a dialog allowing the
 * user to load an image. To make sure the right image is loaded the user can
 * show the test image using the "Show Test" button.
 * <p/>
 * Next the user has to decide which mask to use. The user can use one of the
 * predefined masks or load a custom mask. The predefined mask will create a
 * mask image that splits the original image in squares of the selected size,
 * from 1x1 (pixel) to 100x100 size squares. If the user selected "Load Mask" a
 * dialog will appear allowing the user to load an image to be used as a mask.
 * Only the first band of this mask image wil be used! The user can check the
 * mask by clicking on the "Show Mask" button. Some masks however don't work
 * well with certain comparison methods (for instance pixel (or 1x1) does not
 * work with histogram test.
 * <p/>
 * Next the user has to select which test to use. The user can choose one of the
 * following test: <dl> <dt>Subtract</dt> <dd>For each region and band, as
 * defined by the mask, this routine will calculate the average for both the
 * original and the test and subtract them from each other. The result image
 * will contain the difference in that region. Setting the mask to 1x1 will
 * result in the difference per pixel for the image and test image.</dd>
 * <dt>Correlation</dt> <dd>This will calculate the Pearson linnear correlation
 * for each band and mask value. Currently only the pearon value is calculated
 * correctly. If the mask is set to pixel ((1x1) it will compute the correlation
 * per band over the whole image. There is no resulting image.</dd>
 * <dt>Histogram</dt> <dd>For each band and region in the mask image, we create
 * a histogram of all values. After normalization we calculate the percentage of
 * pixels that are placed in one bin, but should be in another. The resulting
 * image will have this percentage.</dd> <dt>Chi Square RGB and Chi Square
 * HSV</dt> <dd>This will compute the CHI-Square value on a per band and mask
 * region. The resulting CHI-Square value will be returned in the result image.
 * We have observed that for images with small changes (i.e. a red scratch in
 * the image) the CHI-Square value will be large enough to result in a
 * probability of 0.</dd> <dt>Tuple</dt> <dd>This will create a subimage by
 * taking the 3 highest bits of the first, second and the 2 highest bits of the
 * third band and combine them into a 8 bit byte. Next it performs a CHI-Square
 * test for each mask region and band on this subimage and the subimage created
 * from the testimage in simmilar way. The result image will contain the
 * CHI-Square value for each mask region.</dd> <dt>Long Chi Square RGB</dt>
 * <dd>This is the same as Tuple except that instead of only using a few bits we
 * concatenate the first, second and third byte of both images and perform a
 * CHI-Square test on each of these new images. The resulting CHI-Square value
 * will be put in the resulting image.</dd> </dl> <br> Finally the user can use
 * the Multiplier and Image Output fields to select what imagetype the resulting
 * image will be and what multiplier needs to be used when comparing the images.
 * For instance to create an BYTE image of the Histogram we need a multiplier of
 * 255 since the original range of values will be between 0 and 1.
 * <p/>
 * The user can save the result of the comparison as an image using the "Save
 * Image" button.
 *
 * @author Rob Kooper
 */
public class ImageCompareDialog extends Im2LearnFrame implements Im2LearnMenu, ActionListener {
    private JButton _cboxLoadTest = new JButton("Load Test");
    private JButton _cboxShowTest = new JButton("Show Test");
    private JComboBox _comboLoadMask = new JComboBox(new String[]{"1x1", "10x10", "50x50", "100x100",
                                                                  "Load Mask"});
    private JButton _cboxShowMask = new JButton("Show Mask");
    private JLabel _labelmulti = new JLabel("Multiplier");
    private JTextField _textmulti = new JTextField("1.0");
    private JComboBox _comboCompare = new JComboBox(new String[]{"Subtract", "Correlation", "Histogram",
                                                                 "Chi Square RGB", "Chi Square HSV",
                                                                 "Tuple", "Long Chi Square RGB","Housedorff"});
    private JLabel _labeloutput = new JLabel("Image Output");
    private JComboBox _comboTypes = new JComboBox(new String[]{"GUESS", "BYTE", "SHORT", "INT", "LONG",
                                                               "FLOAT", "DOUBLE"});
    private JButton _cboxCompare = new JButton("ImageCompare");
    private JLabel _labelResults = new JLabel("Results");
    private JTextArea _textResults = new JTextArea(10, 25);
    private JButton _cboxSaveImage = new JButton("Save Image");
    private JButton _cboxDone = new JButton("Done");

    private ImageObject _test = null;
    private ImageFrame frmTest = null;
    private ImageObject _mask = null;
    private ImageFrame frmMask = null;
    private ImageObject _compare = null;
    private ImageFrame frmCompare = null;

    private static Log logger = LogFactory.getLog(ImageCompareDialog.class);
    private ImagePanel imagepanel;

    public ImageCompareDialog() {
        super("Image ImageCompare");

        createUI();
    }

    public void closing() {
        if (frmTest != null) {
            frmTest.setImageObject(null);
            frmTest.setVisible(false);
            frmTest = null;
        }
        _test = null;
        if (frmMask != null) {
            frmMask.setImageObject(null);
            frmMask.setVisible(false);
            frmMask = null;
        }
        _mask = null;
        if (frmCompare != null) {
            frmCompare.setImageObject(null);
            frmCompare.setVisible(false);
            frmCompare = null;
        }
        _compare = null;
    }

    protected void createUI() {
        GridBagConstraints gridConstraints = new GridBagConstraints();
        gridConstraints.fill = GridBagConstraints.BOTH;
        gridConstraints.weightx = 1.0;
        gridConstraints.weighty = 0.0;

        GridBagLayout gridBag = new GridBagLayout();
        getContentPane().setLayout(gridBag);

        gridConstraints.gridx = 0;
        gridConstraints.gridy = 0;
        gridConstraints.insets = new Insets(2, 2, 2, 2);
        gridBag.setConstraints(_cboxLoadTest, gridConstraints);
        getContentPane().add(_cboxLoadTest);
        _cboxLoadTest.addActionListener(this);

        gridConstraints.gridx = 1;
        gridBag.setConstraints(_cboxShowTest, gridConstraints);
        getContentPane().add(_cboxShowTest);
        _cboxShowTest.addActionListener(this);

        gridConstraints.gridx = 0;
        gridConstraints.gridy++;
        gridBag.setConstraints(_comboLoadMask, gridConstraints);
        getContentPane().add(_comboLoadMask);
        _comboLoadMask.addActionListener(this);

        gridConstraints.gridx = 1;
        gridBag.setConstraints(_cboxShowMask, gridConstraints);
        getContentPane().add(_cboxShowMask);
        _cboxShowMask.addActionListener(this);

        gridConstraints.gridx = 0;
        gridConstraints.gridy++;
        gridBag.setConstraints(_labelmulti, gridConstraints);
        getContentPane().add(_labelmulti);

        gridConstraints.gridx = 1;
        gridBag.setConstraints(_textmulti, gridConstraints);
        getContentPane().add(_textmulti);

        gridConstraints.gridx = 0;
        gridConstraints.gridy++;
        gridBag.setConstraints(_labeloutput, gridConstraints);
        getContentPane().add(_labeloutput);

        gridConstraints.gridx = 1;
        gridBag.setConstraints(_comboTypes, gridConstraints);
        getContentPane().add(_comboTypes);

        gridConstraints.gridx = 0;
        gridConstraints.gridy++;
        gridBag.setConstraints(_comboCompare, gridConstraints);
        getContentPane().add(_comboCompare);

        gridConstraints.gridx = 1;
        gridBag.setConstraints(_cboxCompare, gridConstraints);
        getContentPane().add(_cboxCompare);
        _cboxCompare.addActionListener(this);

        gridConstraints.gridx = 0;
        gridConstraints.gridy++;
        gridConstraints.gridwidth = 2;
        gridConstraints.insets = new Insets(2, 20, 2, 20);
        gridBag.setConstraints(_labelResults, gridConstraints);
        getContentPane().add(_labelResults);

        gridConstraints.gridy++;
        gridConstraints.weighty = 1.0;
        JScrollPane pane = new JScrollPane(_textResults);
        gridBag.setConstraints(pane, gridConstraints);
        getContentPane().add(pane);

        gridConstraints.gridx = 0;
        gridConstraints.gridy++;
        gridConstraints.weighty = 0.0;
        gridConstraints.gridwidth = 1;
        gridConstraints.insets = new Insets(2, 2, 2, 2);
        gridBag.setConstraints(_cboxSaveImage, gridConstraints);
        getContentPane().add(_cboxSaveImage);
        _cboxSaveImage.addActionListener(this);

        gridConstraints.gridx = 1;
        gridBag.setConstraints(_cboxDone, gridConstraints);
        getContentPane().add(_cboxDone);
        _cboxDone.addActionListener(this);

        pack();
    }

    /**
     * Invoked when an action occurs.
     */
    public void actionPerformed(ActionEvent e) {
        Object cbox = e.getSource();
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        if (cbox.equals(_cboxLoadTest))
            LoadTest();

        else if (cbox.equals(_cboxShowTest))
            ShowTest();

        else if (cbox.equals(_comboLoadMask))
            LoadMask();

        else if (cbox.equals(_cboxShowMask))
            ShowMask();

        else if (cbox.equals(_cboxCompare))
            Compare();

        else if (cbox.equals(_cboxSaveImage))
            SaveImage();

        else if (cbox.equals(_cboxDone))
            setVisible(false);

        setCursor(Cursor.getDefaultCursor());
    }

    private void LoadTest() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select Test Image");
        try {
            String filename = fc.showOpenDialog();
            if (filename != null) {
                _test = ImageLoader.readImage(filename, fc.getFilter());
            }
        } catch (IOException exc) {
            logger.error("Error loading test image.", exc);
        }
        if (frmTest != null)
            ShowTest();
    }

    public void setTestImage(ImageObject test) {
        _test = test;
        if (frmTest.isVisible())
            ShowTest();
    }

    private void ShowTest() {
        if (_test == null) {
            if (frmTest != null) {
                frmTest.setImageObject(null);
                frmTest.setVisible(false);
                frmTest = null;
            }
        } else {
            if (frmTest == null) {
                frmTest = new ImageFrame("Test Image");
            }
            frmTest.setImageObject(_test);
            frmTest.setVisible(true);
        }
    }

    public void setMaskImage(ImageObject mask) {
        _mask = mask;
        if (frmMask.isVisible())
            ShowMask();
    }

    private void LoadMask() {
        if (_comboLoadMask.getSelectedItem().equals("1x1")) {
            ImageObject imgobj = imagepanel.getImageObject();
            _mask = new ImageObjectInt(imgobj.getNumRows(), imgobj.getNumCols(), 1);

            // fill the image with black and white squares of 10x10
            for (int i = 0; i < _mask.getSize(); i++) {
                _mask.set(i, i);
            }


        } else if (_comboLoadMask.getSelectedItem().equals("10x10")) {
            ImageObject imgobj = imagepanel.getImageObject();
            _mask = new ImageObjectInt(imgobj.getNumRows(), imgobj.getNumCols(), 1);

            // fill the image with black and white squares of 10x10
            int rx = (int) Math.ceil(_mask.getNumCols() / 10);
            for (int c = 0; c < _mask.getNumCols(); c++) {
                for (int r = 0; r < _mask.getNumRows(); r++) {
                    _mask.set(r, c, 0, 10 * ((int) (c / 10) + rx * (int) (r / 10)));
                }
            }

        } else if (_comboLoadMask.getSelectedItem().equals("50x50")) {
            ImageObject imgobj = imagepanel.getImageObject();
            _mask = new ImageObjectInt(imgobj.getNumRows(), imgobj.getNumCols(), 1);

            // fill the image with black and white squares of 10x10
            int rx = (int) Math.ceil(_mask.getNumCols() / 50);
            for (int c = 0; c < _mask.getNumCols(); c++) {
                for (int r = 0; r < _mask.getNumRows(); r++) {
                    _mask.set(r, c, 0, 10 * ((int) (c / 50) + rx * (int) (r / 50)));
                }
            }

        } else if (_comboLoadMask.getSelectedItem().equals("100x100")) {
            ImageObject imgobj = imagepanel.getImageObject();
            _mask = new ImageObjectInt(imgobj.getNumRows(), imgobj.getNumCols(), 1);

            // fill the image with black and white squares of 10x10
            int rx = (int) Math.ceil(_mask.getNumCols() / 100);
            for (int c = 0; c < _mask.getNumCols(); c++) {
                for (int r = 0; r < _mask.getNumRows(); r++) {
                    _mask.set(r, c, 0, 10 * ((int) (c / 100) + rx * (int) (r / 100)));
                }
            }

        } else if (_comboLoadMask.getSelectedItem().equals("Load Mask")) {
            FileChooser fc = new FileChooser();
            fc.setTitle("Select Mask Image");
            try {
                String filename = fc.showOpenDialog();
                if (filename != null) {
                    _mask = ImageLoader.readImage(filename, fc.getFilter());
                }
            } catch (IOException exc) {
                logger.error("Error loading mask image.", exc);
            }
        } else {
            _mask = null;
        }
        if (frmMask != null)
            ShowMask();
    }

    private void ShowMask() {
        if (_mask == null) {
            if (frmMask != null) {
                frmMask.setImageObject(null);
                frmMask.setVisible(false);
                frmMask = null;
            }
        } else {
            if (frmMask == null) {
                frmMask = new ImageFrame("Mask Image");
            }
            frmMask.setImageObject(_mask);
            frmMask.setVisible(true);
        }
    }

    private void Compare() {
        String result = "";
        ImageObject imgobj = imagepanel.getImageObject();

        double multiplier = 1.0;
        try {
            multiplier = Double.parseDouble(_textmulti.getText());
        } catch (Exception exc) {
            _textResults.setText(exc.toString());
            exc.printStackTrace();
        }

        ImageCompare imagetest = new ImageCompare();
        imagetest.setResultType((String) _comboTypes.getSelectedItem());
        imagetest.setMultiplier(multiplier);
        //imagetest.setInverse(true);

        // correlation
        if (_comboCompare.getSelectedItem().equals("Correlation")) {

            imagetest.setOriginalImage(imgobj);
            imagetest.setTestImage(_test);
            if (_comboLoadMask.getSelectedItem().equals("1x1")) {
                imagetest.setMaskImage(null);
            } else {
                imagetest.setMaskImage(_mask);
            }

            result = "Correlation Test\n";
            try {
                double[][][] testresults = imagetest.correlation();
                int bands;

                for (int m = 0; m < testresults.length; m++) {
                    bands = testresults[m].length - 1;
                    result += "  mask value " + testresults[m][bands][0] + "\n";
                    result += "  Average Magnitude of Correlations:  : " + testresults[m][bands][1] + "\n";
                    for (int i = 0; i < bands; i++) {
                        result += " band " + i + "\n";
                        result += "  Linear Correlation (or Pearson's r) : " + testresults[m][i][0] + "\n";
                        result += "  Fisher's z transformation           : " + testresults[m][i][1] + "\n";
                        result += "  Degrees of Freedom                  : " + testresults[m][i][2] + "\n";
                        result += "  Student's t probability             : " + testresults[m][i][3] + "\n";
                        result += "  Signal to Noise Ratio               : " + testresults[m][i][4] + "\n";
                    }
                    result += "\n";
                }

            } catch (Exception exc) {
                result += exc.toString();
                exc.printStackTrace();
            }
            _compare = null;
        }else if (_comboCompare.getSelectedItem().equals("Housedorff")) {

                imagetest.setOriginalImage(imgobj);
                imagetest.setTestImage(_test);
                if (_comboLoadMask.getSelectedItem().equals("1x1")) {
                    imagetest.setMaskImage(null);
                } else {
                    imagetest.setMaskImage(_mask);
                }

                result = "Housedorff \n";
                try {
                    double testresults = imagetest.housedorff(imgobj,_test);
                    
            //        for (int m = 0; m < testresults.length; m++){ 
                    	
                            result +="Housedorff distance original and test "+  testresults + "\n";
                          
              //      }
                        result += "\n";
                    }

                 catch (Exception exc) {
                    result += exc.toString();
                    exc.printStackTrace();
                }
                _compare = null;

                // subtract
        } else if (_comboCompare.getSelectedItem().equals("Subtract")) {
            imagetest.setOriginalImage(imgobj);
            imagetest.setTestImage(_test);
            if (_comboLoadMask.getSelectedItem().equals("1x1")) {
                imagetest.setMaskImage(null);
            } else {
                imagetest.setMaskImage(_mask);
            }

            result = "Subtract Test\n";
            try {
                double[][][] testresults = imagetest.subtract();
                int bands;

                for (int m = 0; m < testresults.length; m++) {
                    bands = testresults[m].length - 1;
                    result += " mask val " + testresults[m][bands][0] + "\n";
                    for (int b = 0; b < bands; b++) {
                        result += " band # " + b + "\n";
                        result += "  maximum error     : " + testresults[m][b][0] + "\n";
                        result += "  average error     : " + testresults[m][b][1] + "\n";
                        result += "  Mean Square Error : " + testresults[m][b][2] + "\n";
                        result += "  S/N (inf is good) : " + testresults[m][b][3] + "\n";
                    }
                    result += " average error     : " + testresults[m][testresults[m].length - 1][1] + "\n";
                    result += "\n";
                }

            } catch (Exception exc) {
                result += exc.toString();
                exc.printStackTrace();
            }
            _compare = imagetest.getResultImage();


        } else if (_comboCompare.getSelectedItem().equals("Chi Square RGB") ||
                   _comboCompare.getSelectedItem().equals("Chi Square HSV")) {

            // TODO no HSV
            //            if (_comboCompare.getSelectedItem().equals("Chi Square HSV")) {
            //                ColorModels cm = new ColorModels();
            //                cm.ConvertRGB2HSV(_test);
            //                imagetest.setTestImage(cm.GetConvertedIm().CopyImageObject());
            //                cm.ConvertRGB2HSV(imgobj);
            //                imagetest.setOriginalImage(cm.GetConvertedIm().CopyImageObject());
            //            } else {
            imagetest.setOriginalImage(imgobj);
            imagetest.setTestImage(_test);
            //            }
            if (_comboLoadMask.getSelectedItem().equals("1x1")) {
                imagetest.setMaskImage(null);
            } else {
                imagetest.setMaskImage(_mask);
            }

            try {
                double[][][] testresult = imagetest.chiSquare();

                result += "maximum chi : " + testresult[0][0][0] + "\n";
                result += "chi square  : " + testresult[0][0][1] + "\n";
                result += "mask values : " + testresult[0][0][2] + "\n";
                result += "\n";

                for (int i = 1; i < testresult.length; i++) {
                    for (int b = 0; b < testresult[i].length; b++) {
                        result += testresult[i][b][0] + "\t" + b + "\t";
                        result += testresult[i][b][2] + "\t";
                        result += testresult[i][b][1] + "\t";
                        result += testresult[i][b][3] + "\n";
                    }
                }
            } catch (Exception exc) {
                result += exc.toString();
                exc.printStackTrace();
            }
            _compare = imagetest.getResultImage();

        } else if (_comboCompare.getSelectedItem().equals("Long Chi Square RGB") ||
                   _comboCompare.getSelectedItem().equals("Long Chi Square HSV")) {

            // TODO no HSV
            //            if (_comboCompare.getSelectedItem().equals("Long Chi Square HSV")) {
            //                ColorModels cm = new ColorModels();
            //                cm.ConvertRGB2HSV(_test);
            //                imagetest.setTestImage(cm.GetConvertedIm().CopyImageObject());
            //                cm.ConvertRGB2HSV(imgobj);
            //                imagetest.setOriginalImage(cm.GetConvertedIm().CopyImageObject());
            //            } else {
            imagetest.setOriginalImage(imgobj);
            imagetest.setTestImage(_test);
            //            }
            if (_comboLoadMask.getSelectedItem().equals("1x1")) {
                imagetest.setMaskImage(null);
            } else {
                imagetest.setMaskImage(_mask);
            }

            try {
                double[][][] testresult = imagetest.chiSquareCombined(false);

                result += "maximum chi : " + testresult[0][0][0] + "\n";
                result += "chi square  : " + testresult[0][0][1] + "\n";
                result += "mask values : " + testresult[0][0][2] + "\n";
                result += "\n";

                result += "mask\tchi square\tdof\tprobability\n";
                for (int i = 1; i < testresult.length; i++) {
                    for (int b = 0; b < testresult[i].length; b++) {
                        result += testresult[i][b][0] + "\t" + b + "\t";
                        result += testresult[i][b][2] + "\t";
                        result += testresult[i][b][1] + "\t";
                        result += testresult[i][b][3] + "\n";
                    }
                }
            } catch (Exception exc) {
                result += exc.toString();
                exc.printStackTrace();
            }
            _compare = imagetest.getResultImage();

        } else if (_comboCompare.getSelectedItem().equals("Tuple")) {

            imagetest.setOriginalImage(imgobj);
            imagetest.setTestImage(_test);
            if (_comboLoadMask.getSelectedItem().equals("1x1")) {
                imagetest.setMaskImage(null);
            } else {
                imagetest.setMaskImage(_mask);
            }

            try {
                double[][][] testresult = imagetest.chiSquareSubset();

                result += "maximum chi : " + testresult[0][0][0] + "\n";
                result += "chi square  : " + testresult[0][0][1] + "\n";
                result += "mask values : " + testresult[0][0][2] + "\n";
                result += "\n";

                for (int i = 1; i < testresult.length; i++) {
                    for (int b = 0; b < testresult[i].length; b++) {
                        result += testresult[i][b][0] + "\t" + b + "\t";
                        result += testresult[i][b][2] + "\t";
                        result += testresult[i][b][1] + "\t";
                        result += testresult[i][b][3] + "\n";
                    }
                }
            } catch (Exception exc) {
                result += exc.toString();
                exc.printStackTrace();
            }
            _compare = imagetest.getResultImage();

        } else if (_comboCompare.getSelectedItem().equals("Histogram")) {

            imagetest.setOriginalImage(imgobj);
            imagetest.setTestImage(_test);
            if (_comboLoadMask.getSelectedItem().equals("1x1")) {
                imagetest.setMaskImage(null);
            } else {
                imagetest.setMaskImage(_mask);
            }

            try {
                double[][][] testresult = imagetest.histogram();

                result += "maximum error : " + testresult[0][0][0] + "\n";
                result += "total error   : " + testresult[0][0][1] + "\n";
                result += "mask values   : " + testresult[0][0][2] + "\n";
                result += "\n";

                for (int i = 1; i < testresult.length; i++) {
                    for (int b = 0; b < testresult[i].length; b++) {
                        result += testresult[i][b][0] + "\t" + b + "\t";
                        result += testresult[i][b][1] + "\n";
                    }
                    result += "total error in mask " + testresult[i][0][0] + " is " + testresult[i][0][2] + "\n";
                }
            } catch (Exception exc) {
                result += exc.toString();
                exc.printStackTrace();
            }
            _compare = imagetest.getResultImage();
        }

        // done
        _textResults.setText(result);
        if (_compare == null) {
            if (frmCompare != null) {
                frmCompare.setImageObject(null);
                frmCompare.setVisible(false);
                frmCompare = null;
            }
        } else {
            if (frmCompare == null) {
                frmCompare = new ImageFrame("ImageCompare Result");
            }
            frmCompare.setImageObject(_compare);
            frmCompare.setVisible(true);
        }
    }

    private void SaveImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save Resulting Image");
        try {
            String filename = fc.showSaveDialog();
            if (filename != null) {
                ImageLoader.writeImage(filename, _compare);
            }
        } catch (IOException exc) {
            logger.error("Error saving result.", exc);
        }
    }

    /**
     * This function will return the image that is the result of comparing the
     * test image with the original image with regards to the mask.
     *
     * @return the resulting image with error per pixel.
     */
    public ImageObject getResult() {
        return _compare;
    }

    // ------------------------------------------------------------------------
    // Im2LearnMenu implementation
    // ------------------------------------------------------------------------
    public void setImagePanel(ImagePanel imagepanel) {
        this.imagepanel = imagepanel;
    }

    public JMenuItem[] getPanelMenuItems() {
        return null;
    }

    public JMenuItem[] getMainMenuItems() {
        JMenu tools = new JMenu("Tools");

        JMenuItem compare = new JMenuItem(new AbstractAction("Image Compare") {
            public void actionPerformed(ActionEvent e) {
                if (!isVisible()) {
                    LoadMask();
                    Window win = SwingUtilities.getWindowAncestor(imagepanel);
                    setLocationRelativeTo(win);
                    setVisible(true);
                }
                toFront();
            }
        });
        tools.add(compare);

        return new JMenuItem[]{tools};
    }

    public void imageUpdated(ImageUpdateEvent event) {
    }

    public URL getHelp(String menu) {
        return getClass().getResource("help/ImageCompare.html");
    }
}
