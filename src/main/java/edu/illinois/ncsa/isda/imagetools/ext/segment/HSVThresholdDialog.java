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
package edu.illinois.ncsa.isda.imagetools.ext.segment;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Random;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.datatype.SubArea;
import edu.illinois.ncsa.isda.imagetools.core.display.Im2LearnFrame;
import edu.illinois.ncsa.isda.imagetools.core.display.Im2LearnMainFrame;
import edu.illinois.ncsa.isda.imagetools.core.display.Im2LearnMenu;
import edu.illinois.ncsa.isda.imagetools.core.display.ImageMarker;
import edu.illinois.ncsa.isda.imagetools.core.display.ImagePanel;
import edu.illinois.ncsa.isda.imagetools.core.display.ImageUpdateEvent;
import edu.illinois.ncsa.isda.imagetools.core.io.FileChooser;
import edu.illinois.ncsa.isda.imagetools.core.io.ImageLoader;
import edu.illinois.ncsa.isda.imagetools.ext.calculator.ImageCalculatorDialog;
import edu.illinois.ncsa.isda.imagetools.ext.conversion.ChangeTypeDialog;
import edu.illinois.ncsa.isda.imagetools.ext.conversion.GrayScaleDialog;
import edu.illinois.ncsa.isda.imagetools.ext.conversion.PCADialog;
import edu.illinois.ncsa.isda.imagetools.ext.conversion.RegistrationDialog;
import edu.illinois.ncsa.isda.imagetools.ext.info.CoordValDialog;
import edu.illinois.ncsa.isda.imagetools.ext.info.DebugDialog;
import edu.illinois.ncsa.isda.imagetools.ext.info.InfoDialog;
import edu.illinois.ncsa.isda.imagetools.ext.misc.PlotComponent;
import edu.illinois.ncsa.isda.imagetools.ext.panel.AnnotationDialog;
import edu.illinois.ncsa.isda.imagetools.ext.panel.CropDialog;
import edu.illinois.ncsa.isda.imagetools.ext.panel.GammaDialog;
import edu.illinois.ncsa.isda.imagetools.ext.panel.SelectBandDialog;
import edu.illinois.ncsa.isda.imagetools.ext.panel.SelectionDialog;
import edu.illinois.ncsa.isda.imagetools.ext.panel.UseTotalsDialog;
import edu.illinois.ncsa.isda.imagetools.ext.panel.ZoomDialog;
import edu.illinois.ncsa.isda.imagetools.ext.statistics.Histogram;
import edu.illinois.ncsa.isda.imagetools.ext.statistics.HistogramDialog;
import edu.illinois.ncsa.isda.imagetools.ext.statistics.ImageCompareDialog;
import edu.illinois.ncsa.isda.imagetools.ext.vis.FakeRGBColorDialog;
import edu.illinois.ncsa.isda.imagetools.ext.vis.ImageExtractDialog;
import edu.illinois.ncsa.isda.imagetools.ext.vis.PlayBandDialog;
import edu.illinois.ncsa.isda.imagetools.ext.vis.PseudoImageDialog;

/**
 * 
 * 
 * The class HSVThreshold is a tool for clustering problems in HSV (Hue,
 * Saturation, and Value) color space for color-based pixel separation. The main
 * purpose of this tool is to support semi-automated color-based segmentation.
 * Particularly, this tool provides users the decision support about valid pixel
 * value ranges for specific types of object detection.<br>
 * <br>
 * Description: The class operates in three ranges of HSV values. Points within
 * the user-specified value ranges are appeared in the result image, both as RGB
 * and HSV images. In each scrollbar, a user can adjust the upper and lower
 * limits of the pixel values, and can fix the dynamic value range by checking
 * the "Fix Range" option. For example, the lower/upper limit will move
 * automatically while keeping the same dynamic value range when changing the
 * upper/lower limit.<br>
 * <br>
 * Setup: By default, both RGB and HSV image are displayed from the main
 * Im2Learn frame. The two windows dynamically show the classified images when a
 * user adjusts the threshold value ranges. For large image processing, such as
 * "svs" format, a user can directly load the image into the HSV threshold tool
 * independently from the main Im2Learn frame by pressing "Load" button. If a
 * sub-sampled overview image is available, as a same filename with "tif"
 * extension, an overview window will be displayed where a user can
 * interactively select the currently displayed region (marked as red cross).<br>
 * <br>
 * Run: First, select a sub-region from the overview image to load a 200 by 200
 * pixel neighborhood from the large image to the RGB and HSV windows. Next,
 * change the Hue, Saturation, and Value range to acquire the thresholded image.
 * The number of valid (displayed) pixels is shown in the title of the tool as
 * "Positive pixel count" with the percentage with respect to the total number
 * of the pixels in the sub-region.<br>
 * "Change Region" button can be used to load an overview image with different
 * filename. Note that the overview image has to be a 1:200 sub-sampled image
 * from the original svs image, for example, an image sub-sampled by the factor
 * of 200.
 * 
 * 
 */

public class HSVThresholdDialog extends Im2LearnFrame implements ActionListener, AdjustmentListener, MouseMotionListener, MouseListener, Im2LearnMenu {
    HSVThreshold       thres;
    JButton            bt_changeRegion, bt_save, bt_done, bt_load;
    JTextField         HFieldUp, SFieldUp, VFieldUp;
    JTextField         HFieldLow, SFieldLow, VFieldLow;
    JScrollBar         Hscrolls[]            = new JScrollBar[2];
    JScrollBar         Sscrolls[]            = new JScrollBar[2];
    JScrollBar         Vscrolls[]            = new JScrollBar[2];
    JButton            bt_hist_H, bt_hist_S, bt_hist_V;

    JTextArea          HInfo, SInfo, VInfo;

    JCheckBox          HrangeFix, SrangeFix, VrangeFix;
    int                HRange, SRange, VRange;
    static ImageTile   loader;

    MI2LearnSubFrame   HSVFrame, RGBFrame, overviewF;;
    String             overviewImageFilename = null;
    int                subX, subY;
    int                tileWidth             = 200, tileHeight = 200;
    ImagePanel         imagepanel;
    ImageMarker        marker;
    long               posPixCnt;

    static private Log logger                = LogFactory.getLog(HSVThresholdDialog.class);

    public HSVThresholdDialog() {
        createUI();
    }

    public HSVThresholdDialog(ImageTile load) {
        createUI();
        setLoader(load);
    }

    public HSVThresholdDialog(ImageObject rgbIm) {
        createUI();
        setImage(rgbIm);
    }

    void setLoader(ImageTile load) {

        loader = load;
        thres = new HSVThreshold();

        if (RGBFrame != null) {
            RGBFrame.dispose();
        }
        if (HSVFrame != null) {
            HSVFrame.dispose();
        }

        RGBFrame = new MI2LearnSubFrame();
        //HSVFrame = new MI2LearnSubFrame();
        HSVFrame = new MI2LearnSubFrame();

        RGBFrame.setSize(tileWidth * 2, tileHeight * 2);
        HSVFrame.setSize(tileWidth * 2, tileHeight * 2);

        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        RGBFrame.setLocation(0, d.height - RGBFrame.getSize().height - 30);
        HSVFrame.setLocation(RGBFrame.getLocation().x + RGBFrame.getWidth(), RGBFrame.getLocation().y);
        RGBFrame.setVisible(true);
        HSVFrame.setVisible(true);
        setRGBImageObject(loader.getTileImage(0, 0));

        try {
            loadOverview();
        } catch (Exception e) {
            System.err.println("The overview file not available.");
            //e.printStackTrace();
        }
        RGBFrame.setTitle("RGB Image");
        HSVFrame.setTitle("HSV Image");
    }

    void setImage(ImageObject rgbIm) {

        //		HFieldUp.setText(String.valueOf(Hscrolls[0].getValue()));
        //		HFieldLow.setText(String.valueOf(Hscrolls[1].getValue()));
        //		SFieldUp.setText(String.valueOf(Sscrolls[0].getValue()/100F));
        //		SFieldLow.setText(String.valueOf(Sscrolls[1].getValue()/100F));
        //		VFieldUp.setText(String.valueOf(Vscrolls[0].getValue()));
        //		VFieldLow.setText(String.valueOf(Vscrolls[1].getValue()));

        HFieldUp.setText("  360");
        HFieldLow.setText("     0");
        SFieldUp.setText("  1.0");
        SFieldLow.setText("  0.0");
        VFieldUp.setText(" 255");
        VFieldLow.setText("     0");

        Hscrolls[0].setEnabled(true);
        Hscrolls[1].setEnabled(true);
        Sscrolls[0].setEnabled(true);
        Sscrolls[1].setEnabled(true);
        Vscrolls[0].setEnabled(true);
        Vscrolls[1].setEnabled(true);

        HrangeFix.setEnabled(true);
        SrangeFix.setEnabled(true);
        VrangeFix.setEnabled(true);

        HFieldUp.setEnabled(true);
        HFieldLow.setEnabled(true);
        SFieldUp.setEnabled(true);
        SFieldLow.setEnabled(true);
        VFieldUp.setEnabled(true);
        VFieldLow.setEnabled(true);

        thres = new HSVThreshold();

        if (RGBFrame != null) {
            RGBFrame.dispose();
        }
        if (HSVFrame != null) {
            HSVFrame.dispose();
        }

        RGBFrame = new MI2LearnSubFrame();
        //HSVFrame = new MI2LearnSubFrame();
        HSVFrame = new MI2LearnSubFrame();

        RGBFrame.setSize(400, 400);
        HSVFrame.setSize(400, 400);

        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        RGBFrame.setLocation(0, d.height - RGBFrame.getSize().height - 30);
        HSVFrame.setLocation(RGBFrame.getLocation().x + RGBFrame.getWidth(), RGBFrame.getLocation().y);
        RGBFrame.setVisible(true);
        HSVFrame.setVisible(true);
        setRGBImageObject(rgbIm);

        try {
            loadOverview();
        } catch (Exception e) {
            System.err.println("The overview file not available.");
            //	e.printStackTrace();
        }

        RGBFrame.setTitle("RGB Image");
        HSVFrame.setTitle("HSV Image");
    }

    public void setRGBImageObject(ImageObject rgbIm) {
        try {
            thres.setRGBImage(rgbIm);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ImageObject rgb = thres.getRGBSource();
        rgb.setMaxInCoreSize(Integer.MAX_VALUE);
        ImageObject hsv = thres.getHSVSource();
        hsv.setMaxInCoreSize(Integer.MAX_VALUE);

        RGBFrame.getImagePanel().setImageObject(rgb);
        HSVFrame.getImagePanel().setImageObject(hsv);
    }

    private void createUI() {

        JPanel HSVPanel = new JPanel();

        JPanel Hpanel = new JPanel(new GridLayout(3, 1));
        Hpanel.setBorder(BorderFactory.createTitledBorder("Hue"));
        //		Hscrolls[0] = new JScrollBar(JScrollBar.HORIZONTAL, (int)thres.getHSVSource().getMin(0)+2, 1, Math.min((int)thres.getHSVSource().getMin(0),-1), Math.max((int)thres.getHSVSource().getMax(0),366));
        //		Hscrolls[1] = new JScrollBar(JScrollBar.HORIZONTAL, (int)thres.getHSVSource().getMin(0), 1, Math.min((int)thres.getHSVSource().getMin(0),-1), Math.max((int)thres.getHSVSource().getMax(0),366));
        //	
        Hscrolls[0] = new JScrollBar(JScrollBar.HORIZONTAL, 360, 1, 0, 361);
        Hscrolls[1] = new JScrollBar(JScrollBar.HORIZONTAL, 0, 1, 0, 361);

        Hscrolls[0].setPreferredSize(new Dimension(200, 20));
        Hscrolls[1].setPreferredSize(new Dimension(200, 20));

        Hscrolls[0].addAdjustmentListener(this);
        Hscrolls[1].addAdjustmentListener(this);
        //Hscrolls[0].setValue(10);

        HFieldUp = new JTextField("      360");
        HFieldLow = new JTextField("        0");

        HFieldUp.addActionListener(this);
        HFieldLow.addActionListener(this);
        HFieldUp.addMouseListener(this);
        HFieldLow.addMouseListener(this);

        JPanel HPU = new JPanel();
        HPU.add(new JLabel("Upper Limit"));
        HPU.add(Hscrolls[0]);
        HPU.add(HFieldUp);

        JPanel HPL = new JPanel();
        HPL.add(new JLabel("Lower Limit"));
        HPL.add(Hscrolls[1]);
        HPL.add(HFieldLow);

        HrangeFix = new JCheckBox("Fix Range");
        HrangeFix.addActionListener(this);

        bt_hist_H = new JButton("Show Histogram");
        bt_hist_H.addActionListener(this);

        HInfo = new JTextArea(11, 20);
        HInfo.setEditable(false);

        JPanel optP = new JPanel();
        optP.add(HrangeFix);
        optP.add(bt_hist_H);

        Hpanel.add(HPU);
        Hpanel.add(HPL);
        Hpanel.add(optP);
        JPanel Hpanel2 = new JPanel(new BorderLayout());
        Hpanel2.add(Hpanel, BorderLayout.NORTH);
        Hpanel2.add(new JScrollPane(HInfo, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS), BorderLayout.SOUTH);

        JPanel Spanel = new JPanel(new GridLayout(3, 1));
        Spanel.setBorder(BorderFactory.createTitledBorder("Saturation"));
        //		Sscrolls[0] = new JScrollBar(JScrollBar.HORIZONTAL, (int)(thres.getHSVSource().getMax(1)*100)-10, 1, Math.min((int)(thres.getHSVSource().getMin(1)*100),0), Math.max((int)(thres.getHSVSource().getMax(1)*100),100));
        //		Sscrolls[1] = new JScrollBar(JScrollBar.HORIZONTAL, (int)(thres.getHSVSource().getMin(1)*100)+10, 1, Math.min((int)(thres.getHSVSource().getMin(1)*100),0), Math.max((int)(thres.getHSVSource().getMax(1)*100),100));

        Sscrolls[0] = new JScrollBar(JScrollBar.HORIZONTAL, 100, 1, 0, 101);
        Sscrolls[1] = new JScrollBar(JScrollBar.HORIZONTAL, 0, 1, 0, 101);

        Sscrolls[0].addAdjustmentListener(this);
        Sscrolls[1].addAdjustmentListener(this);

        Sscrolls[0].setPreferredSize(new Dimension(200, 20));
        Sscrolls[1].setPreferredSize(new Dimension(200, 20));

        SFieldUp = new JTextField("     100");
        SFieldLow = new JTextField("        0");
        SFieldUp.addActionListener(this);
        SFieldLow.addActionListener(this);
        SFieldUp.addMouseListener(this);
        SFieldLow.addMouseListener(this);

        JPanel SPU = new JPanel();
        SPU.add(new JLabel("Upper Limit"));
        SPU.add(Sscrolls[0]);
        SPU.add(SFieldUp);

        JPanel SPL = new JPanel();
        SPL.add(new JLabel("Lower Limit"));
        SPL.add(Sscrolls[1]);
        SPL.add(SFieldLow);

        SrangeFix = new JCheckBox("Fix Range");
        SrangeFix.addActionListener(this);

        bt_hist_S = new JButton("Show Histogram");
        bt_hist_S.addActionListener(this);

        SInfo = new JTextArea(11, 20);
        SInfo.setEditable(false);

        JPanel optPS = new JPanel();
        optPS.add(SrangeFix);
        optPS.add(bt_hist_S);

        Spanel.add(SPU);
        Spanel.add(SPL);
        Spanel.add(optPS);

        JPanel Spanel2 = new JPanel(new BorderLayout());
        Spanel2.add(Spanel, BorderLayout.NORTH);
        Spanel2.add(new JScrollPane(SInfo, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS), BorderLayout.SOUTH);

        JPanel Vpanel = new JPanel(new GridLayout(3, 1));
        Vpanel.setBorder(BorderFactory.createTitledBorder("Value"));
        //		Vscrolls[0] = new JScrollBar(JScrollBar.HORIZONTAL, (int)thres.getHSVSource().getMax(2)-10, 1, Math.min((int)thres.getHSVSource().getMin(2),0), Math.max((int)thres.getHSVSource().getMax(2), 255));
        //		Vscrolls[1] = new JScrollBar(JScrollBar.HORIZONTAL, (int)thres.getHSVSource().getMin(2)+10, 1, Math.min((int)thres.getHSVSource().getMin(2),0), Math.max((int)thres.getHSVSource().getMax(2), 255));
        Vscrolls[0] = new JScrollBar(JScrollBar.HORIZONTAL, 255, 1, 0, 256);
        Vscrolls[1] = new JScrollBar(JScrollBar.HORIZONTAL, 0, 1, 0, 256);

        Vscrolls[0].addAdjustmentListener(this);
        Vscrolls[1].addAdjustmentListener(this);

        Vscrolls[0].setPreferredSize(new Dimension(200, 20));
        Vscrolls[1].setPreferredSize(new Dimension(200, 20));

        VFieldUp = new JTextField("     255");
        VFieldLow = new JTextField("        0");

        VFieldUp.addActionListener(this);
        VFieldLow.addActionListener(this);
        VFieldUp.addMouseListener(this);
        VFieldLow.addMouseListener(this);

        JPanel VPU = new JPanel();
        VPU.add(new JLabel("Upper Limit"));
        VPU.add(Vscrolls[0]);
        VPU.add(VFieldUp);

        JPanel VPL = new JPanel();
        VPL.add(new JLabel("Lower Limit"));
        VPL.add(Vscrolls[1]);
        VPL.add(VFieldLow);

        VrangeFix = new JCheckBox("Fix Range");
        VrangeFix.addActionListener(this);

        bt_hist_V = new JButton("Show Histogram");
        bt_hist_V.addActionListener(this);

        VInfo = new JTextArea(11, 20);
        VInfo.setEditable(false);

        JPanel optPV = new JPanel();
        optPV.add(VrangeFix);
        optPV.add(bt_hist_V);

        Vpanel.add(VPU);
        Vpanel.add(VPL);
        Vpanel.add(optPV);
        JPanel Vpanel2 = new JPanel(new BorderLayout());
        Vpanel2.add(Vpanel, BorderLayout.NORTH);
        Vpanel2.add(new JScrollPane(VInfo, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS), BorderLayout.SOUTH);

        HSVPanel.add(Hpanel2);
        HSVPanel.add(Spanel2);
        HSVPanel.add(Vpanel2);

        getContentPane().add(HSVPanel, BorderLayout.NORTH);

        bt_load = new JButton("Load");
        bt_load.addActionListener(this);

        bt_changeRegion = new JButton("change region");
        bt_changeRegion.addActionListener(this);

        bt_save = new JButton("save");
        bt_save.addActionListener(this);
        bt_save.setEnabled(false);

        bt_done = new JButton("done");
        bt_done.addActionListener(this);

        JPanel btPanel = new JPanel();
        btPanel.add(bt_load);
        btPanel.add(bt_changeRegion);
        //btPanel.add(bt_save);
        btPanel.add(bt_done);

        getContentPane().add(btPanel, BorderLayout.SOUTH);
        this.setTitle("HSV Threshold");

        Hscrolls[0].setEnabled(false);
        Hscrolls[1].setEnabled(false);
        Sscrolls[0].setEnabled(false);
        Sscrolls[1].setEnabled(false);
        Vscrolls[0].setEnabled(false);
        Vscrolls[1].setEnabled(false);

        HrangeFix.setEnabled(false);
        SrangeFix.setEnabled(false);
        VrangeFix.setEnabled(false);

        HFieldUp.setEnabled(false);
        HFieldLow.setEnabled(false);
        SFieldUp.setEnabled(false);
        SFieldLow.setEnabled(false);
        VFieldUp.setEnabled(false);
        VFieldLow.setEnabled(false);
        bt_changeRegion.setEnabled(false);

        pack();
        this.setResizable(false);
    }

    public void loadOverview() throws Exception {

        String svsFilename = loader.getInputFilename();
        overviewImageFilename = svsFilename.substring(0, svsFilename.length() - 3) + "tif";

        File ovF = new File(overviewImageFilename);
        if (ovF.canRead()) {
            if (overviewF != null) {
                overviewF.dispose();
            }

            overviewF = new MI2LearnSubFrame(overviewImageFilename);
            overviewF.setLocation(0, this.getHeight());
            overviewF.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            overviewF.getImagePanel().setSelectionAllowed(false);
            overviewF.setVisible(true);
            overviewF.setTitle("Overview Image");
            overviewF.getImagePanel().addMouseMotionListener(this);
            overviewF.getImagePanel().addMouseListener(this);
        } else {
            overviewImageFilename = null;
        }
    }

    public Histogram buildHistogram(ImageObject im, int band) throws Exception {
        im.setMaxInCoreSize(Integer.MAX_VALUE);

        int subWidth = 200, subHeight = 200;
        int xx, yy;

        long now = (new Date().getTime());
        Random generator = new Random(now);
        SubArea area = new SubArea();
        area.setSize(subWidth, subHeight);

        Histogram hist = new Histogram();

        long[] sumData = null;
        int[] data = null;
        ImageObject subim = null;
        subim.setMaxInCoreSize(Integer.MAX_VALUE);

        int[] avgData = null;

        int i;
        if (im.getNumCols() * im.getNumRows() > 1000000) {
            // this is the case when the image is very large and 
            // we have to sample the image and average sub-area histograms
            for (i = 0; i < 100; i++) {
                xx = generator.nextInt(im.getNumCols() - subWidth);
                yy = generator.nextInt(im.getNumRows() - subHeight);
                area.setCol(xx);
                area.setRow(yy);

                //	System.out.println(area);

                subim = im.crop(area);

                //	new MI2Learn(subim);

                hist.SetMinDataVal(subim.getMin(band));
                hist.SetMaxDataVal(subim.getMax(band));
                hist.SetNumBins(1000);

                hist.Hist(im, band);
                hist.Stats();
                data = hist.GetHistData();

                //			if(sumData == null) {
                //				sumData = new long[data.length];
                //			}
                //			
                //			for(int j = 0; j<data.length; j++) {
                //				sumData[j] = sumData[j] + data[j];
                //			}

                if (avgData == null) {
                    avgData = new int[data.length];
                }

                double residual = 0;
                double convergeCoeff = 0;
                double pv;
                for (int j = 0; j < avgData.length; j++) {
                    residual = (avgData[j] - data[j]) * (avgData[j] - data[j]);
                    pv = avgData[j];

                    avgData[j] = (int) ((double) avgData[j] * i / (i + 1) + (double) data[j] / (double) (i + 1));

                    convergeCoeff = (avgData[j] - pv) * (avgData[j] - pv);
                }
                residual = Math.sqrt(residual / (i + 1));
                convergeCoeff = Math.sqrt(convergeCoeff / (i + 1));

                System.out.println(i + 1 + " " + residual + " " + convergeCoeff);

                //	int[] retData = hist.GetHistData();
                //		retData = avgData;
                //		hist.Stats();

                //showHistogramDebug(i, hist);
            }
        } else {
            // this is the case when the histogram is computed from the whole image
            hist.SetMinDataVal(im.getMin(band));
            hist.SetMaxDataVal(im.getMax(band));
            hist.SetNumBins(1000);

            hist.Hist(im, band);
            hist.Stats();

        }

        return hist;
    }

    void showHistogramDebug(int idx, Histogram hist) {
        PlotComponent pc = new PlotComponent();
        int id = pc.addSeries("Band " + idx);

        int[] data = hist.GetHistData();
        double x = hist.GetMinDataVal();
        for (int j = 0; j < data.length; j++, x += hist.GetWideBins()) {
            pc.setValue(id, x, (double) data[j] / (double) hist.GetNumSamples());
        }
        Im2LearnFrame frame = new Im2LearnFrame("Histogram (PDF) Plot " + idx);
        frame.getContentPane().add(pc);
        frame.pack();

        String output = "Contrast  = " + hist.GetContrast() + "\n" + "Count     = " + hist.GetCount() + "\n" + "Energy    = " + hist.GetEnergy() + "\n" + "Entropy   = " + hist.GetEntropy() + "\n"
                + "Kurtosis  = " + hist.GetKurtosis() + "\n" + "Lower %   = " + hist.GetLowerPercentile() + "\n" + "Upper %   = " + hist.GetUpperPercentile() + "\n" + "Skew      = " + hist.GetSkew()
                + "\n" + "Mean      = " + hist.GetMean() + "\n" + "Median    = " + hist.GetMedian() + "\n" + "S Dev     = " + hist.GetSDev();

        Point loc = this.getLocation();

        int band = 0;
        switch (band) {
        case 0:
            HInfo.setText(output);
            break;
        case 1:
            SInfo.setText(output);
            break;
        case 2:
            VInfo.setText(output);
            break;
        }

        frame.setLocation(loc.x + (int) ((float) getWidth() * band / 3), loc.y + this.getHeight());
        frame.setVisible(true);

    }

    void showHistogram(Histogram hist, int band) {
        PlotComponent pc = new PlotComponent();
        int id = pc.addSeries("Band " + band);

        int[] data = hist.GetHistData();

        double x = hist.GetMinDataVal();
        for (int j = 0; j < data.length; j++, x += hist.GetWideBins()) {
            pc.setValue(id, x, (double) data[j] / (double) hist.GetNumSamples());
        }

        Im2LearnFrame frame = new Im2LearnFrame("Histogram (PDF) Plot - band " + band);
        frame.getContentPane().add(pc);
        frame.pack();

        String output = "Contrast  = " + hist.GetContrast() + "\n" + "Count     = " + hist.GetCount() + "\n" + "Energy    = " + hist.GetEnergy() + "\n" + "Entropy   = " + hist.GetEntropy() + "\n"
                + "Kurtosis  = " + hist.GetKurtosis() + "\n" + "Lower %   = " + hist.GetLowerPercentile() + "\n" + "Upper %   = " + hist.GetUpperPercentile() + "\n" + "Skew      = " + hist.GetSkew()
                + "\n" + "Mean      = " + hist.GetMean() + "\n" + "Median    = " + hist.GetMedian() + "\n" + "S Dev     = " + hist.GetSDev() + "\n" + "MinDataVal     = " + hist.GetMinDataVal() + "\n"
                + "MaxDataVal     = " + hist.GetMaxDataVal() + "\n" + "NumBins     = " + hist.GetNumBins() + "\n" + "BinWidth     = " + hist.GetWideBins();

        Point loc = this.getLocation();
        switch (band) {
        case 0:
            HInfo.setText(output);
            break;
        case 1:
            SInfo.setText(output);
            break;
        case 2:
            VInfo.setText(output);
            break;
        }

        frame.setLocation(loc.x + (int) ((float) getWidth() * band / 3), loc.y + this.getHeight());
        frame.setVisible(true);

    }

    public Histogram getHistogram(int band) {

        Histogram hist = thres.getHSVHistogram(band);
        hist.Stats();
        return hist;

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();

        if (obj == bt_changeRegion) {
            if (loader == null) {
                System.err.println("Load the image First");
            }

            if ((overviewF != null) && overviewF.isVisible()) {
                return;
            }

            if (overviewImageFilename == null) {
                FileChooser fc = new FileChooser();
                fc.setTitle("Overview Image");
                try {
                    overviewImageFilename = fc.showOpenDialog();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                if (overviewImageFilename == null) {
                    return;
                }
            }

            overviewF = new MI2LearnSubFrame(overviewImageFilename);
            overviewF.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            overviewF.setLocation(0, this.getHeight());
            overviewF.getImagePanel().setSelectionAllowed(false);
            overviewF.setVisible(true);
            overviewF.setTitle("Overview Image");
            overviewF.getImagePanel().addMouseMotionListener(this);
            overviewF.getImagePanel().addMouseListener(this);

            //	f.getImagePanel().setImageObject(load.downSampleImage());
            //f.setVisible(true);
            //			String coordx = JOptionPane.showInputDialog("Input sub-region for x");
            //			String coordy = JOptionPane.showInputDialog("Input sub-region for y");
            //			
            //			System.out.println(coordx+"  "+coordy);
            //			try{
            //				setRGBImageObject(load.getTileImage(Integer.parseInt(coordx), Integer.parseInt(coordy)));
            //			}
            //			catch(Exception ee) {
            //				System.err.println("Invalid number format");
            //				ee.printStackTrace();
            //			}
        } else if (obj == bt_load) {
            try {
                FileChooser fc = new FileChooser();
                fc.addOpenFilter(new String[] { "svs" }, "Scanscope Image File");
                String f;
                f = fc.showOpenDialog();
                if (f == null) {
                    return;
                }
                if (f.endsWith("svs")) {
                    ImageTile load = new ImageTile(f);
                    load.setTileSize(tileWidth, tileHeight);
                    setLoader(load);
                } else {
                    ImageObject im = ImageLoader.readImage(f);
                    setImage(im);
                }
                bt_changeRegion.setEnabled(true);

            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

        } else if (obj == bt_save) {

        } else if (obj == bt_done) {
            if (overviewF != null) {
                overviewF.dispose();
            }
            if (RGBFrame != null) {
                RGBFrame.dispose();
            }
            if (HSVFrame != null) {
                HSVFrame.dispose();
            }
            this.dispose();

        } else if (obj == HrangeFix) {
            if (HrangeFix.isSelected()) {
                HRange = Hscrolls[0].getValue() - Hscrolls[1].getValue();
                if (HRange < 0) {
                    logger.warn("The range must be positive. Setting 0");
                    HRange = 0;
                }
            }
        } else if (obj == SrangeFix) {
            if (SrangeFix.isSelected()) {
                SRange = Sscrolls[0].getValue() - Sscrolls[1].getValue();
                if (SRange < 0) {
                    logger.warn("The range must be positive. Setting 0");
                    SRange = 0;
                }
            }
        } else if (obj == VrangeFix) {
            if (VrangeFix.isSelected()) {
                VRange = Vscrolls[0].getValue() - Vscrolls[1].getValue();
                if (VRange < 0) {
                    logger.warn("The range must be positive. Setting 0");
                    VRange = 0;
                }
            }
        }

        else if (obj == HFieldUp) {
            Hscrolls[0].setValue(Integer.parseInt(HFieldUp.getText()));
        } else if (obj == HFieldLow) {
            Hscrolls[1].setValue(Integer.parseInt(HFieldLow.getText()));
        } else if (obj == SFieldUp) {
            Sscrolls[0].setValue((int) (Float.parseFloat(SFieldUp.getText()) * 100));
        } else if (obj == SFieldLow) {
            Sscrolls[1].setValue((int) (Float.parseFloat(SFieldLow.getText()) * 100));
        } else if (obj == VFieldUp) {
            Vscrolls[0].setValue(Integer.parseInt(VFieldUp.getText()));
        } else if (obj == VFieldLow) {
            Vscrolls[1].setValue(Integer.parseInt(VFieldLow.getText()));
        }

        else if (obj == bt_hist_H) {
            try {
                showHistogram(buildHistogram(thres.getHSVResult(), 0), 0);
            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            //	showHistogram(getHistogram(0),0);
        } else if (obj == bt_hist_S) {
            try {
                showHistogram(buildHistogram(thres.getHSVResult(), 1), 1);
            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            //			showHistogram(getHistogram(1),1);
        } else if (obj == bt_hist_V) {
            try {
                showHistogram(buildHistogram(thres.getHSVResult(), 2), 2);
            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            //showHistogram(getHistogram(2),2);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.AdjustmentListener#adjustmentValueChanged(java.awt.event.AdjustmentEvent)
     */
    public void adjustmentValueChanged(AdjustmentEvent e) {
        Object obj = e.getSource();
        if (obj == Hscrolls[0]) {
            if (HrangeFix.isSelected()) {
                if (Hscrolls[0].getValue() - HRange < Hscrolls[1].getMinimum()) {
                    Hscrolls[1].setValue(Hscrolls[1].getMinimum());
                } else {
                    Hscrolls[1].setValue(Hscrolls[0].getValue() - HRange);
                }
            }
            HFieldUp.setText(String.valueOf(Hscrolls[0].getValue()));
            HFieldLow.setText(String.valueOf(Hscrolls[1].getValue()));
        } else if (obj == Hscrolls[1]) {
            if (HrangeFix.isSelected()) {
                if (Hscrolls[1].getValue() + HRange > Hscrolls[0].getMaximum()) {
                    Hscrolls[0].setValue(Hscrolls[0].getMaximum());
                } else {
                    Hscrolls[0].setValue(Hscrolls[1].getValue() + HRange);
                }
            }
            HFieldUp.setText(String.valueOf(Hscrolls[0].getValue()));
            HFieldLow.setText(String.valueOf(Hscrolls[1].getValue()));

        }

        else if (obj == Sscrolls[0]) {
            if (SrangeFix.isSelected()) {
                if (Sscrolls[0].getValue() - SRange < Sscrolls[1].getMinimum()) {
                    Sscrolls[1].setValue(Sscrolls[1].getMinimum());
                } else {
                    Sscrolls[1].setValue(Sscrolls[0].getValue() - SRange);
                }
            }
            SFieldUp.setText(String.valueOf(Sscrolls[0].getValue() / 100F));
            SFieldLow.setText(String.valueOf(Sscrolls[1].getValue() / 100F));
        } else if (obj == Sscrolls[1]) {
            if (SrangeFix.isSelected()) {
                if (Sscrolls[1].getValue() + SRange > Sscrolls[0].getMaximum()) {
                    Sscrolls[0].setValue(Sscrolls[0].getMaximum());
                } else {
                    Sscrolls[0].setValue(Sscrolls[1].getValue() + SRange);
                }
            }
            SFieldUp.setText(String.valueOf(Sscrolls[0].getValue() / 100F));
            SFieldLow.setText(String.valueOf(Sscrolls[1].getValue() / 100F));
        }

        else if (obj == Vscrolls[0]) {
            if (VrangeFix.isSelected()) {
                if (Vscrolls[0].getValue() - VRange < Vscrolls[1].getMinimum()) {
                    Vscrolls[1].setValue(Vscrolls[1].getMinimum());
                } else {
                    Vscrolls[1].setValue(Vscrolls[0].getValue() - VRange);
                }
            }
            VFieldUp.setText(String.valueOf(Vscrolls[0].getValue()));
            VFieldLow.setText(String.valueOf(Vscrolls[1].getValue()));
        } else if (obj == Vscrolls[1]) {
            if (VrangeFix.isSelected()) {
                if (Vscrolls[1].getValue() + VRange > Vscrolls[0].getMaximum()) {
                    Vscrolls[0].setValue(Vscrolls[0].getMaximum());
                } else {
                    Vscrolls[0].setValue(Vscrolls[1].getValue() + VRange);
                }
            }
            VFieldUp.setText(String.valueOf(Vscrolls[0].getValue()));
            VFieldLow.setText(String.valueOf(Vscrolls[1].getValue()));
        }

        posPixCnt = thres.threshold((Hscrolls[0].getValue()), (float) (Hscrolls[1].getValue()), Sscrolls[0].getValue() / 100F, Sscrolls[1].getValue() / 100F, (float) Vscrolls[0].getValue(),
                (float) Vscrolls[1].getValue());

        RGBFrame.getImagePanel().setImageObject(thres.getRGBResult());
        HSVFrame.getImagePanel().setImageObject(thres.getHSVResult());

        int size = thres.getRGBResult().getNumCols() * thres.getRGBResult().getNumCols();
        String infotxt = "Positive Pixel Count: " + String.valueOf(posPixCnt) + " (" + String.valueOf(100F * (float) posPixCnt / (float) size) + "%)";
        this.setTitle("HSV Threshold  " + "[" + infotxt + "]");
    }

    public void mouseClicked(MouseEvent arg0) {
        Object obj = arg0.getSource();

        if (obj == HFieldUp) {
            HFieldUp.setText("");
        } else if (obj == HFieldLow) {
            HFieldLow.setText("");
        } else if (obj == SFieldUp) {
            SFieldUp.setText("");
        } else if (obj == SFieldLow) {
            SFieldLow.setText("");
        } else if (obj == VFieldUp) {
            VFieldUp.setText("");
        } else if (obj == VFieldLow) {
            VFieldLow.setText("");
        }

    }

    public void mouseEntered(MouseEvent arg0) {
    }

    public void mouseExited(MouseEvent arg0) {
    }

    public void mousePressed(MouseEvent arg0) {
        if (arg0.getButton() == MouseEvent.BUTTON1) {
            Object obj = arg0.getSource();
            if ((overviewF != null) && (obj == overviewF.getImagePanel())) {
                Point p = overviewF.getImagePanel().getMouseLocation(arg0.getPoint());
                subX = p.x;
                subY = p.y;
                if (marker == null) {
                    marker = new ImageMarker(subX, subY, 10, ImageMarker.CROSS);
                    marker.setColor(Color.red);
                    overviewF.getImagePanel().addAnnotationImage(marker);
                    marker.setVisible(true);
                } else {
                    overviewF.getImagePanel().removeAllAnnotations();
                    marker.setLocation(subX, subY);
                    overviewF.getImagePanel().addAnnotationImage(marker);
                }

                try {
                    setRGBImageObject(loader.getTileImage(subX, subY));
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
            }
        }
    }

    public void mouseReleased(MouseEvent arg0) {
    }

    static public void main(String args[]) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            LogFactory.getLog("").error("Unable to load native look and feel");
        }

        HSVThresholdDialog svs = new HSVThresholdDialog();
        svs.setVisible(true);

    }

    // ------------------------------------------------------------------------
    // Im2LearnMenu implementation
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * 
     * @see ncsa.im2learn.core.display.Im2LearnMenu#getHelp(java.lang.String)
     */
    public URL getHelp(String menu) {
        return this.getClass().getResource("help/HSVThreshold.html");
    }

    /*
     * (non-Javadoc)
     * 
     * @see ncsa.im2learn.core.display.Im2LearnMenu#getMainMenuItems()
     */
    public JMenuItem[] getMainMenuItems() {
        JMenu tools = new JMenu("Tools");
        JMenu thres = new JMenu("Threshold");

        thres.add(new JMenuItem(new AbstractAction("HSV Threshold") {
            public void actionPerformed(ActionEvent e) {
                setImage(imagepanel.getImageObject());
                HSVThresholdDialog.this.setVisible(true);
            }
        }));
        tools.add(thres);
        return new JMenuItem[] { tools };
    }

    /*
     * (non-Javadoc)
     * 
     * @see ncsa.im2learn.core.display.Im2LearnMenu#getPanelMenuItems()
     */
    public JMenuItem[] getPanelMenuItems() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ncsa.im2learn.core.display.Im2LearnMenu#setImagePanel(ncsa.im2learn.core.display.ImagePanel)
     */
    public void setImagePanel(ImagePanel imagepanel) {
        this.imagepanel = imagepanel;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ncsa.im2learn.core.display.ImageUpdateListener#imageUpdated(ncsa.im2learn.core.display.ImageUpdateEvent)
     */
    public void imageUpdated(ImageUpdateEvent event) {
        if (event.getId() == ImageUpdateEvent.NEW_IMAGE) {
            if (isVisible()) {
                this.setImage(imagepanel.getImageObject());
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
     */
    public void mouseDragged(MouseEvent arg0) {
        Object obj = arg0.getSource();
        if (obj == overviewF.getImagePanel()) {
            Point p = overviewF.getImagePanel().getMouseLocation(arg0.getPoint());
            subX = p.x;
            subY = p.y;
            if (marker == null) {
                marker = new ImageMarker(subX, subY, 10, ImageMarker.CROSS);
                marker.setColor(Color.red);
                overviewF.getImagePanel().addAnnotationImage(marker);
                marker.setVisible(true);
            } else {
                overviewF.getImagePanel().removeAllAnnotations();
                marker.setLocation(subX, subY);
                overviewF.getImagePanel().addAnnotationImage(marker);
            }

            try {
                setRGBImageObject(loader.getTileImage(subX, subY));
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
     */
    public void mouseMoved(MouseEvent arg0) {
        // TODO Auto-generated method stub

    }

    class MI2LearnSubFrame extends Im2LearnMainFrame {
        /**
         * Create the MainFrame with the menus
         */

        public MI2LearnSubFrame() {
            super();
        }

        public MI2LearnSubFrame(ImageObject image) {
            super(image);
        }

        public MI2LearnSubFrame(String filename) {
            super(filename);
        }

        public void addMenus() {
            // allow selection
            getImagePanel().setSelectionAllowed(true);

            // information dialogs
            addMenu(new InfoDialog());
            addMenu(new PlayBandDialog());
            addMenu(new PseudoImageDialog());
            addMenu(new CoordValDialog());

            addMenu(new ImageExtractDialog());

            // zoom
            addMenu(new ZoomDialog());

            // selection dialogs
            addMenu(new SelectionDialog());
            addMenu(new CropDialog());

            // markers
            addMenu(new AnnotationDialog());

            // color change
            addMenu(new GammaDialog());
            addMenu(new SelectBandDialog());
            addMenu(new UseTotalsDialog());
            addMenu(new GrayScaleDialog());
            addMenu(new FakeRGBColorDialog());

            // change image type
            addMenu(new ChangeTypeDialog());

            // perform PCA
            addMenu(new PCADialog());

            // image comparison
            addMenu(new ImageCompareDialog());
            addMenu(new HistogramDialog());
            addMenu(new ImageCalculatorDialog());
            addMenu(new Seg2DSuperDialog());
            addMenu(new Seg2DBallDialog());
            addMenu(new Seg2DDialog());
            addMenu(new ThresholdDialog());
            addMenu(new BoundBoxDialog());
            addMenu(new HSVThresholdDialog());

            // image registration
            addMenu(new RegistrationDialog());

            // debug
            addMenu(new DebugDialog());

        }

    }

}
