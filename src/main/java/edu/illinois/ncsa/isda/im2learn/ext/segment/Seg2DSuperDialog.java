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
package edu.illinois.ncsa.isda.im2learn.ext.segment;

import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.NumberFormatter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImPoint;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageException;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.display.*;
import edu.illinois.ncsa.isda.im2learn.core.io.FileChooser;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageLoader;
import edu.illinois.ncsa.isda.im2learn.ext.info.InfoDialog;
import edu.illinois.ncsa.isda.im2learn.ext.misc.ImEnhance;
import edu.illinois.ncsa.isda.im2learn.ext.misc.ImageFrame;


import java.awt.BorderLayout;


import java.awt.GridLayout;
import java.awt.Point;

import java.awt.Window;
import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.text.ParseException;

import java.util.Hashtable;
import java.util.Enumeration;


/**

<html>
  <head>
    <title>Segment 2D Super</title>
  </head>
  <body>
    <h1>
      Segment 2D Super
    </h1>
    <p>   
	  The class Seg2DSuperDialog provides a graphics user interface 
	  (GUI) to the class Seg2DSuper designed for region growing 
	  segmentation tasks given a set of initial spatial locations. The 
	  purpose of this tool is  to create masks in a supervised way for 
	  scene modeling purposes.
	</p>
	<p>
	While any unsupervised segmentation algorithm has to estimate the 
	optimal number (or numbers) of image segments, their spatial 
	locations and intensity profile characteristics, this tool allows 
	a user 
	(1) to specify initial spatial locations (seeds) of desired image 
	segments (regions) by a mouse click in the original or mask 
	image, (2) to select minimum similarity of interior pixels 
	forming a contiguous segment, (3) to remove holes from segments 
	that are introduced due to speckle noise or other sharp intensity 
	discontinuities, (4) to assign higher level labels to each formed 
	segment (e.g., grass or sand), and (5) to merge segments that are 
	heterogeneous in terms of intensity profile but belong to the 
	same segment conceptually. Next, we describe how to form a mask 
	and take an advantage of the aforementioned capabilities using 
	this tool.
	</p>

	<p>
	  The dialog of the supervised segmentation tool is shown below.
	</p>

	<img src="seg2DSuperDialog.jpg" width="466" height="626">
 
	<p>
	<B> Setup and Run:</B>
	After launching the dialog, a user will see a new frame 
	displaying the current mask with only one label. By clicking 
	anywhere in the original image and pressing the button "Segment 
	From Original", the region growing algorithm will form a 
	contiguous region (segment) that contains pixels with intensity 
	values less different from the mouse selected one than the value 
	of a threshold. The threshold value can be modified by moving a 
	slider bar denoted as Delta or by entering a new value in the 
	edit box denoted as Value in the Threshold area of the dialog.
	The distance between any two pixels is computed using a Euclidean 
	distance measure.
    </p>
	<p>
	Let us assume that we try to segment the image of a runner shown below.
	</p>
	<img src="imSeg2DSuper0.jpg" width="393" height="263">
	<p>
	If the obtained segment is satisfactory then it can be added to 
	the final segmentation by clicking on the "Add Change to Final" 
	button. The radio buttons "Intermediate Mask" and "Final Mask" 
	allow switching the mask visualization between the intermediate 
	and final segmentation results.
    </p>
	<p>
	It is possible (and sometimes desirable) to select the region 
	growing origin in the mask image. One would mouse click in the 
	mask image at a selected pixel seed location and then press the 
	button "Segment From Mask" to obtain an intermediate segmentation 
	results starting from the given location.
	An example of intermediate segmentation is shown below.
    </p>
    
	<img src="imSeg2DSuper4.jpg" width="285" height="241">

	<p>
	A user might want to annotate each segment with its abstract 
	meaning since the abstract meaning is usually very hard to obtain 
	directly from image segment characteristics.
	This is enabled by typing a name in the "Name" edit box for the 
	associated segment described by a value in the "Value" edit box 
	of the "Label" dialog area. With the drop down menu inside of the 
	"Label" dialog area, one can select already used labels and 
	modified their names. In the "Auto Increment" mode, the values 
	will increase by one and the assigned labels will be set to 
	"unlabeled". Segment names and their pixel counts are reported in 
	the text area as it is shown in the dialog snapshot above.
    </p>
    
	<p>
	In the presence of speckle noise, the obtained segments might
	have undesirable holes since the noise pixels are outside of the 
	allowed range of values and increasing the threshold value would 
	lead to merging multiple segments of different abstract meanings. 
	The "Noise" part of the dialog allows performing noise removal 
	(or removal of segment holes). The "Size Threshold" value denotes 
	the maximum size of a hole that would be considered for removal. 
	The "Half Window" value denotes a half size of a spatial kernel 
	(neighborhood of pixels) that is used for filling the exiting 
	holes of size less than the "Size Threshold" value. This is 
	critical when holes occur at the border of two labeled segments. 
	The "Iteration" slider bar is for choosing the number of 
	iterations that the noise removal step should be applied. Two 
	examples below show a segment with holes and without holes after 
	noise removal.
    </p>

	<img src="imSeg2DSuper1.jpg" width="285" height="241">
	<img src="imSeg2DSuper2.jpg" width="285" height="241">

	<p>
	It is often the case that a desired segment contains 
	heterogeneous intensities and cannot be created by choosing a 
	single threshold and a pixel seed, for example the hands of a 
	runner in the sample image presented above (see obtained labels 
	below).
    </p>
	<p>
	In this case, merging of multiple segments can be achieved by 
	selecting a label in the "Label" dialog area, choosing
	a pixel seed location and adding the obtained regions into one 
	segment. Although this procedure can lead to segments that are 
	not spatially contiguous, the formation of such segments is 
	supported because of higher level abstract labels might need such 
	representation.
    </p>

	<img src="imSeg2DSuper3.jpg" width="285" height="241">

	<p>
	If the original image contains more than one band then the 
	Euclidean distance threshold for region growing can be replaced 
	by a vector of values containing one threshold per band. This 
	choice of a threshold vector as opposed to a threshold value can 
	be achieved by switching the drop down menu from "Sphere 
	(Radius)" to "Box (Dimension)" in the Threshold area of the 
	dialog. The slider bar "Use Bands" is enabled in the "Box 
	(Dimension)" mode and different threshold values can be set for 
	each band. This option could be used, for instance, if it is 
	desired to obtain a segment from a RGB image that contains red 
	color pixels (red band threshold is set to 0) with a range of 
	green and blue shades (green and blue band thresholds are set to 
	50).
    </p>
	<p>
	Finally, the resulting mask image can be saved using the "Save" 
	button. If the output file format is HDF (.h5 suffix) then the 
	label names will be stored as well, otherwise the information 
	will be lost. A mask from previous sessions can be reloaded by 
	clicking the "Load" button for further modifications. If the 
	results during a current session are not satisfactory then the 
	button "Reset" will reset the mask for a new segmentation 
	attempt. Pressing the button "Done" will close the dialog.

	</p>
	<p>
	<B>
	Release notes:</B>

	@author Peter Bajcsy & Rob Kooper & Peter Ferak
	@version 2.0
	</p>
</body>
</html>


*/
public class Seg2DSuperDialog extends Im2LearnFrame implements Im2LearnMenu {
    private JComboBox           cmbLabel          = new JComboBox();
    private NumberFormatter     formatterValue    = new NumberFormatter(NumberFormat.getIntegerInstance());
    private JFormattedTextField txtLabelValue     = new JFormattedTextField(formatterValue);
    private JTextField          txtLabelName      = new JTextField("");
    private JComboBox           cmbType           = new JComboBox();
    private NumberFormatter     formatterDelta    = new NumberFormatter(NumberFormat.getNumberInstance());
    private JFormattedTextField txtDelta          = new JFormattedTextField(formatterDelta);
    private JSlider             sldDelta          = new JSlider(1, 100, 1);
    private JLabel              lblSizeThreshhold = new JLabel("9", JLabel.TRAILING);
    private JSlider             sldSizeThreshhold = new JSlider(2, 1000, 9);
    private JLabel              lblHalfWindow     = new JLabel("1", JLabel.TRAILING);
    private JSlider             sldHalfWindow     = new JSlider(1, 200, 1);
    private JLabel              lblIterations     = new JLabel("1", JLabel.TRAILING);
    private JSlider             sldIterations     = new JSlider(1, 100, 1);
    private JLabel              lblBand           = new JLabel("1", JLabel.TRAILING);
    private JSlider             sldBand           = new JSlider(1, 3, 1);
    private JRadioButton        radIntermediate   = new JRadioButton("Intermediate Mask");
    private JRadioButton        radFinal          = new JRadioButton("Final Mask");
    private JRadioButton        radMaskImage      = new JRadioButton("Mask and Image");
    private JTextArea           txtMessage        = new JTextArea(10, 40);
    private ImageFrame          frmMask           = null;
    private ImageObject         imgMaskInter      = null;
    private ImageObject         imgMaskFinal      = null;
    private ImageObject 		deltaimg		  = null;
    private ImPoint             impoint           = new ImPoint(-1, -1);
    private Seg2DSuper          seg2dsuper        = new Seg2DSuper();
    private double[][]          minmax            = new double[2][2];
    private ImEnhance           imenhance         = new ImEnhance();
    private double[]            deltas            = null;
    private final int 			MAX_INT 		  = 2147483647;// 2^(31) -1 
    private final int 			MIN_INT 		  = -MAX_INT;
    private final float 		MAX_FLOAT 		  = 3.40282347E+38F;
    private final float 		MIN_FLOAT 		  = -MAX_FLOAT;
    private final double 		MAX_DOUBLE 		  = 1.79769313486231570E+308;
    private final double 		MIN_DOUBLE 		  = -MAX_DOUBLE;

    private int                 UNLABELED         = 1;
    private String              AUTO_INCREMENT    = "<Auto Incremement>";
    private String              THR_SPHERE        = "Sphere (Radius)";
    private String              THR_BOX           = "Box (Dimension)";
    
    private static Log logger = LogFactory.getLog(Seg2DSuperDialog.class);
	private ImagePanel 			imagepanel;
	
    public Seg2DSuperDialog() {
        super("Segment 2D Super");

        createUI();
    }
    
    protected void createUI() {
        JPanel       pnl, pnltmp, pnlbox;
        ButtonGroup  group;
        Box          vbox;

        pnl = new JPanel(new GridLayout(1, 2));
        vbox = Box.createVerticalBox();

        pnlbox = new JPanel(new GridLayout(3, 1));
        pnlbox.setBorder(BorderFactory.createTitledBorder("Select"));
        pnlbox.add(new JButton(new AbstractAction("Segment From Original") {
            public void actionPerformed(ActionEvent e) {
                selectOriginalPoint();
            }

        }));
        pnlbox.add(new JButton(new AbstractAction("Segment From Mask") {
            public void actionPerformed(ActionEvent e) {
                selectMaskPoint();
            }
        }));
        pnlbox.add(new JButton(new AbstractAction("Apply Changes to Final") {
            public void actionPerformed(ActionEvent e) {
                applyPointToMask();
            }
        }));
        vbox.add(pnlbox);
        vbox.add(Box.createGlue());

        pnlbox = new JPanel(new GridLayout(4, 1));
        pnlbox.setBorder(BorderFactory.createTitledBorder("Label"));
        pnlbox.add(cmbLabel);
        cmbLabel.removeAllItems();
        addLabel(0, AUTO_INCREMENT, true);
        addLabel(UNLABELED, "NOT LABELED", false);
        cmbLabel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectLabel();
            }
        });
        pnltmp = new JPanel(new GridLayout(1, 2));
        pnltmp.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        pnltmp.add(new JLabel("Value"));
        formatterValue.setMinimum(new Integer(1));
        formatterValue.setMaximum(new Integer(Integer.MAX_VALUE));
        pnltmp.add(txtLabelValue);
        txtLabelValue.setEditable(false);
        pnlbox.add(pnltmp);
        pnltmp = new JPanel(new GridLayout(1, 2));
        pnltmp.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        pnltmp.add(new JLabel("Name"));
        pnltmp.add(txtLabelName);
        pnlbox.add(pnltmp);
        pnlbox.add(new JButton(new AbstractAction("Modify Selected Label") {
            public void actionPerformed(ActionEvent e) {
                modifyLabel();
            }
        }));
        vbox.add(pnlbox);
        vbox.add(Box.createGlue());

        pnlbox = new JPanel(new GridLayout(5, 1));
        pnlbox.setBorder(BorderFactory.createTitledBorder("Threshhold"));
        pnltmp = new JPanel(new GridLayout(1, 2));
        pnltmp.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        pnltmp.add(new JLabel("Delta"));
        formatterDelta.setMinimum(new Double(1.0));
        formatterDelta.setMaximum(new Double(100.0));
        pnltmp.add(txtDelta);
        txtDelta.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    txtDelta.commitEdit();
                } catch (ParseException exc) {
                    exc.printStackTrace();
                }
                Number num = (Number)txtDelta.getValue();
                sldDelta.setValue(num.intValue());
                if (sldBand.isEnabled()) {
                    int band = sldBand.getValue() - 1;
                    deltas[band] = num.doubleValue();
                }
                segmentImage();
            }
        });
        pnlbox.add(pnltmp);
        pnlbox.add(sldDelta);
        sldDelta.setMinorTickSpacing(1);
        sldDelta.setMajorTickSpacing(10);
        sldDelta.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (!sldDelta.getValueIsAdjusting() && sldDelta.hasFocus()) {
                    double val = sldDelta.getValue();
                    txtDelta.setText(val + "");
                    if (sldBand.isEnabled()) {
                        int band = sldBand.getValue() - 1;
                        deltas[band] = val;
                    }
                    segmentImage();
                }
            }
        });
        pnltmp = new JPanel(new GridLayout(1, 2));
        pnltmp.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        pnltmp.add(new JLabel("Use Band"));
        pnltmp.add(lblBand);
        pnlbox.add(pnltmp);
        pnlbox.add(sldBand);
        sldBand.setMinorTickSpacing(1);
        sldBand.setMajorTickSpacing(10);
        sldBand.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (!sldBand.getValueIsAdjusting()) {
                    int band = sldBand.getValue();
                    lblBand.setText(band + "");
                    band--;
                    txtDelta.setValue(new Double(deltas[band]));
                    sldDelta.setValue((int)deltas[band]);

                }
            }
        });
        sldBand.setEnabled(false);
        cmbType.addItem(THR_SPHERE);
        cmbType.addItem(THR_BOX);
        pnlbox.add(cmbType);
        cmbType.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (cmbType.getSelectedItem().equals(THR_SPHERE)) {
                    sldBand.setEnabled(false);
                } else {
                    sldBand.setEnabled(true);
                }
                modifySlider(false);
                segmentImage();
            }
        });
        vbox.add(pnlbox);

        pnl.add(vbox);
        vbox = Box.createVerticalBox();

        pnlbox = new JPanel(new GridLayout(3, 1));
        pnlbox.setBorder(BorderFactory.createTitledBorder("Show"));
        group = new ButtonGroup();
        group.add(radIntermediate);
        pnlbox.add(radIntermediate);
        radIntermediate.setSelected(true);
        radIntermediate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (radIntermediate.isSelected()) {
                    showMask();
                }
            }
        });
        group.add(radFinal);
        pnlbox.add(radFinal);
        radFinal.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (radFinal.isSelected()) {
                    showMask();
                }
            }
        });
        group.add(radMaskImage);
        pnlbox.add(radMaskImage);
        radMaskImage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (radMaskImage.isSelected()) {
                    showMask();
                }
            }
        });
        radMaskImage.setEnabled(false);
        vbox.add(pnlbox);
        vbox.add(Box.createGlue());

        pnlbox = new JPanel(new GridLayout(7, 1));
        pnlbox.setBorder(BorderFactory.createTitledBorder("Noise"));
        pnltmp = new JPanel(new GridLayout(1, 2));
        pnltmp.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        pnltmp.add(new JLabel("Size Threshold"));
        pnltmp.add(lblSizeThreshhold);
        pnlbox.add(pnltmp);
        pnlbox.add(sldSizeThreshhold);
        sldSizeThreshhold.setMinorTickSpacing(1);
        sldSizeThreshhold.setMajorTickSpacing(10);
        sldSizeThreshhold.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (!sldSizeThreshhold.getValueIsAdjusting()) {
                    lblSizeThreshhold.setText(sldSizeThreshhold.getValue() + "");
                }
            }
        });
        pnltmp = new JPanel(new GridLayout(1, 2));
        pnltmp.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        pnltmp.add(new JLabel("Half Window"));
        pnltmp.add(lblHalfWindow);
        pnlbox.add(pnltmp);
        pnlbox.add(sldHalfWindow);
        sldHalfWindow.setMinorTickSpacing(1);
        sldHalfWindow.setMajorTickSpacing(10);
        sldHalfWindow.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (!sldHalfWindow.getValueIsAdjusting()) {
                    lblHalfWindow.setText(sldHalfWindow.getValue() + "");
                }
            }
        });
        pnltmp = new JPanel(new GridLayout(1, 2));
        pnltmp.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        pnltmp.add(new JLabel("Iterations"));
        pnltmp.add(lblIterations);
        pnlbox.add(pnltmp);
        pnlbox.add(sldIterations);
        sldIterations.setMinorTickSpacing(1);
        sldIterations.setMajorTickSpacing(10);
        sldIterations.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (!sldIterations.getValueIsAdjusting()) {
                    lblIterations.setText(sldIterations.getValue() + "");
                }
            }
        });
        pnlbox.add(new JButton(new AbstractAction("Remove Noise") {
            public void actionPerformed(ActionEvent e) {
                removeNoise();
            }
        }));
        vbox.add(pnlbox);

        pnl.add(vbox);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(pnl, BorderLayout.NORTH);

        txtMessage.setEditable(false);
        JScrollPane scrollpane = new JScrollPane(txtMessage, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        getContentPane().add(scrollpane, BorderLayout.CENTER);

        pnl = new JPanel();
        pnl.add(new JButton(new AbstractAction("Reset") {
            public void actionPerformed(ActionEvent e) {
                reset();
            }
        }));
        pnl.add(new JButton(new AbstractAction("Load") {
            public void actionPerformed(ActionEvent e) {
                load();
            }
        }));
        pnl.add(new JButton(new AbstractAction("Save") {
            public void actionPerformed(ActionEvent e) {
                save();
            }
        }));
        pnl.add(new JButton(new AbstractAction("Done") {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                done();
            }
        }));
        pnl.add(new JButton(new AbstractAction("Apply") {
            public void actionPerformed(ActionEvent e) {
                imagepanel.setImageObject(imgMaskFinal);
            }
        }));
        getContentPane().add(pnl, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                done();
            }
        });
        pack();
    }

	/**
	 * 
	 */
	protected void done() {
        if (frmMask != null) {
            frmMask.setVisible(false);
        }
	}

	/**
	 * 
	 */
	protected void save() {
	       // copy the labels into properties
        String[] labels = new String[cmbLabel.getItemCount()-1];
        String    tmp;
        int       i, j;
        for(j=0, i=0; i<cmbLabel.getItemCount(); i++) {
            tmp = (String)cmbLabel.getItemAt(i);
            if (!tmp.equals(AUTO_INCREMENT)) {
                labels[j++] = tmp;
            }
        }
        imgMaskFinal.setProperty("masklabels", labels);
        
        FileChooser fc = new FileChooser();
        fc.setTitle("Save Mask Image As");
        try {
            String filename = fc.showSaveDialog();
            if (filename != null) {
            	//test
            	if(cmbLabel.getItemCount() <= 255){
            		ImageObject imgMaskFinal1 = ImageObject.createImage(imgMaskFinal.getNumRows(),imgMaskFinal.getNumCols(), imgMaskFinal.getNumBands(), "BYTE");
            		for(int idx = 0; idx < imgMaskFinal.getSize(); idx++){
            			imgMaskFinal1.set(idx, imgMaskFinal.getByte(idx));
            		}
            		imgMaskFinal1.setProperty("masklabels", labels);
                    ImageLoader.writeImage(filename, imgMaskFinal1);
                    imgMaskFinal1 = null;                    
            	}else{
            		ImageLoader.writeImage(filename, imgMaskFinal);
            	}
            }
        } catch (IOException exc) {
            logger.error("Error saving result. (IOException)", exc);
        }
		catch (ImageException exc) {
	        logger.error("Error saving result. (ImageException)", exc);
			}
	}

	/**
	 * 
	 */
	protected void load() {
        int       i, j, v;
        Integer   tmp;
        Hashtable ht = new Hashtable();

        ImageObject img = null;
        FileChooser fc = new FileChooser();
        fc.setTitle("Open");
        try {
            String filename = fc.showOpenDialog();
            if (filename != null) {
                img = ImageLoader.readImage(filename, fc.getFilter());
            }
        } catch (IOException exc) {
            logger.error("Error loading test image.", exc);
        }
        ImageObject org = imagepanel.getImageObject();
		
        // check size
        if ((img.getNumRows() != org.getNumRows()) || (img.getNumCols() != org.getNumCols())) {
            logger.debug("loaded mask wrong size.");
            return;
        }

        // check bands, print warning but continue
        if (img.getNumBands() != 1) {
        	logger.debug("Only using 1st band of multiband image.");
            return;
        }

        // copy the labels into hashtable
        if (img.getProperties().get("masklabels") != null) {
            //String[] labels = (HashMap)img.getProperties();
        	if( !img.getProperties().containsKey("masklabels") ){
        		logger.debug("ERROR: the image does not contain the key masklabels");
        	}
        	String [] labels = (String []) img.getProperties().get("masklabels");
            for(i=0; i<labels.length; i++) {
                String[] parts = labels[i].split(" - ");
                try {
                    tmp = Integer.decode(parts[0]);
                    ht.put(tmp, parts[1]);
                } catch (NumberFormatException exc) {
                    exc.printStackTrace();
                }
            }
        }

        // copy data
        if (img.getData() != null) {
            for(j=0, i=0; i<img.getSize(); i+=img.getNumBands(), j++) {
                v = img.getByte(i) & 0xff;
                imgMaskFinal.setInt(j, v);
                tmp = new Integer(v);
                if (ht.get(tmp) == null) {
                    ht.put(tmp, "unlabeled");
                }
            }
        }else {
            	return;
            }
         
        
        
        /* else if (img.imageShort != null) {
            for(j=0, i=0; i<img.size; i+=img.sampPerPixel, j++) {
                v = img.imageShort[i] & 0xffff;
                imgMaskFinal.imageInt[j] = v;
                tmp = new Integer(v);
                if (ht.get(tmp) == null) {
                    ht.put(tmp, "unlabeled");
                }
            }
        } else if (img.imageInt != null) {
            for(j=0, i=0; i<img.size; i+=img.sampPerPixel, j++) {
                v = img.imageInt[i] & 0xffff;
                imgMaskFinal.imageInt[j] = v;
                tmp = new Integer(v);
                if (ht.get(tmp) == null) {
                    ht.put(tmp, "unlabeled");
                }
            }
        } else {
            return;
        }*/
        System.arraycopy(imgMaskFinal.getData(), 0, imgMaskInter.getData(), 0,
                         imgMaskFinal.getSize());

        // list mask values
        cmbLabel.removeAllItems();
        addLabel(0, AUTO_INCREMENT, true);
        addLabel(UNLABELED, "NOT LABELED", false);
        for(Enumeration keys=ht.keys(); keys.hasMoreElements(); ) {
            tmp = (Integer)keys.nextElement();
            addLabel(tmp.intValue(), (String)ht.get(tmp), false);
        }

        // finally show the new mask
        showMask();
	}

	/**
	 * 
	 */
	protected void reset() {
	      ImageObject img = imagepanel.getImageObject();

	        if(img == null){
	        	logger.error("Error: no image to process !");
	            return;
	        }

	        if ((img.getData() == null))
	        	/*&& (img.imageShort == null) && (img.imageInt == null) &&
	            (img.imageFloat == null) && (img.imageDouble == null))*/ 
	        {
	        	logger.error("Error: no BYTE or SHORT or FLOAT or DOUBLE image to process !");
	            return;
	        }

	        cmbLabel.removeAllItems();
	        addLabel(0, AUTO_INCREMENT, true);
	        addLabel(UNLABELED, "NOT LABELED", false);

	         /////////////////////////
	         // prepare efficient thresholding
	        if (seg2dsuper.myThresh.findMeanDistance(img) <= 0 ){     
	          logger.error("Error: could not compute Mean Distance !");
	          return;
	        }

	        sldSizeThreshhold.setValue(seg2dsuper.getSizeThresh());
	        sldHalfWindow.setValue(seg2dsuper.getHalfWindow());

	       // imgMaskInter = new ImageObject(img.getNumRows(), img.getNumCols(), 1, "INT");
	       // imgMaskFinal = new ImageObject(img.getNumRows(), img.getNumCols(), 1, "INT");
	        try{
	         imgMaskInter = ImageObject.createImage(img.getNumRows(), img.getNumCols(), 1, ImageObject.TYPE_INT);
	         imgMaskFinal = ImageObject.createImage(img.getNumRows(), img.getNumCols(), 1, ImageObject.TYPE_INT);
	        }catch(Exception e){
	        	logger.error("ERROR: could not allocate memory for mask objects");
	        	return;
	        }
	        	        
	        for( int i = 0; i < imgMaskInter.getSize(); i++)
	        {
	        imgMaskInter.set(i, UNLABELED);
	        }	       
	        for( int i = 0; i < imgMaskFinal.getSize(); i++)
	        {
	        imgMaskFinal.set(i, UNLABELED);
	        }	  
	        //Arrays.fill(imgMaskInter.setData(UNLABELED), UNLABELED);
        	//Arrays.fill(imgMaskFinal.imageInt, UNLABELED);
	        
	        deltas = new double[img.getNumBands()];

	        impoint.x = -1;
	        impoint.y = -1;

	        txtMessage.setText("Mask Label\tCount\n");
	        txtMessage.append(UNLABELED + "\t" + imgMaskInter.getSize() + "\n");

	        findMinMax(img);
	        modifySlider(true);
	        showMask();
		
	}

	/**
	 * 
	 */
	protected void removeNoise() {
	      if (!checkMaskValid()) {
            return;
        }

        // TODO should allow user to select if only new mask have noise removed
        //      this might involve re-segmenting image.
        if (!cmbLabel.getSelectedItem().equals(AUTO_INCREMENT)) {
            if (JOptionPane.showConfirmDialog(this, "Removing all noise from mask, including mask areas already added to final!",
                    "Are you sure", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }
        }

        // don't remove all, I will loop
        seg2dsuper.setRemoveAll(false);

        // copy paramters from UI
        seg2dsuper.setHalfWindow(sldHalfWindow.getValue());
        seg2dsuper.setSizeThresh(sldSizeThreshhold.getValue());

        int lbl = currentMaskValue();

        int loop = sldIterations.getValue();
        do {
			if (seg2dsuper.noiseRemoval(imgMaskInter, lbl)) {
			    txtMessage.append("NoiseRemoval : " + seg2dsuper.getNumRemoved() + "\n");
			} else {
			    txtMessage.append("NoiseRemoval : FAILED\n");
			}
			loop--;
        } while((loop > 0) && (seg2dsuper.getNumRemoved() > 0));
        showMask();
	}

	/**
	 * 
	 */
	protected void showMask() {
        checkMaskValid();

        
        ImageObject mask = imgMaskInter;

        if (radFinal.isSelected()) {
            mask = imgMaskFinal;
        }

        if (imenhance.EnhanceLabelsIn(mask, -1, true)) {
            mask = imenhance.GetEnhancedObject();
        }

        if (frmMask == null) {
            frmMask = new ImageFrame("Mask");
            frmMask.setImageObject(mask);
            frmMask.getImagePanel().addMenu(new InfoDialog());
            frmMask.getImagePanel().setFakeRGBcolor(true);
        } else {
            frmMask.setImageObject(mask);
            frmMask.getImagePanel().setFakeRGBcolor(true);
        }

       frmMask.setVisible(true);
	}

	/**
	 * @param b
	 */
	protected void modifySlider(boolean initDelta) {
        if (!checkMaskValid()) {
            return;
        }

        if (cmbType.getSelectedItem().equals(THR_SPHERE)) {
            if (initDelta) {
                double val = (minmax[0][1] - minmax[0][0]) * 0.2;
                txtDelta.setValue(new Double(val));
                sldDelta.setValue((int)val);
            }
            sldDelta.setMinimum((int)minmax[0][0]);
            sldDelta.setMaximum((int)minmax[0][1]);
            formatterDelta.setMinimum(new Double(minmax[0][0]));
            formatterDelta.setMaximum(new Double(minmax[0][1]));
        } else {
            if (initDelta) {
                double val = (minmax[1][1] - minmax[1][0]) * 0.2;
                txtDelta.setValue(new Double(val));
                sldDelta.setValue((int)val);
                for(int i=0; i<deltas.length; i++) {
                    deltas[i] = val;
                }
            }
            sldDelta.setMinimum((int)minmax[1][0]);
            sldDelta.setMaximum((int)minmax[1][1]);
            formatterDelta.setMinimum(new Double(minmax[1][0]));
            formatterDelta.setMaximum(new Double(minmax[1][1]));
        }
	}

	/**
	 * 
	 */
	protected void segmentImage() {
        int i;

        if (!checkMaskValid() || ((impoint.x == -1) && (impoint.y == -1))) {
            return;
        }

        try {
            txtDelta.commitEdit();
        } catch (Exception exc) {
            exc.printStackTrace();
        }
        ImageObject img   = imagepanel.getImageObject();
        double      delta = ((Number)txtDelta.getValue()).doubleValue();

        // copy the final mask to the mask       
        System.arraycopy(imgMaskFinal.getData(), 0, imgMaskInter.getData(), 0,
                         (int)imgMaskFinal.getSize());

        // perform segmentation
        if (cmbType.getSelectedItem().equals(THR_SPHERE)) {
            seg2dsuper.nonTexture_Sphere(img, imgMaskInter, UNLABELED, impoint,
                                         delta, true);
        } else {
        	//deltaimg = ImageObject.createImage(1,1, deltas.length,);
            //ImageObject deltaimg = new ImageObject(1, 1, deltas.length, deltas);  
          try 
          {  
            deltaimg = ImageObject.createImage(1, 1, deltas.length, "DOUBLE");
            
            for(i=0; i < deltaimg.getSize(); i++)
            {
            deltaimg.set(i, deltas[i]);                         // the for statement is supposed to fill the array with deltas values, in the original version the array is passed, which is not possible here.
            }
            
            seg2dsuper.nonTexture_Box(img, imgMaskInter, UNLABELED, impoint, deltaimg);
        } catch (Exception e) {
        	e.printStackTrace();
        	}
        }

        // set value to value selected
        int maxval = -1;
        for(i=0; i<imgMaskInter.getSize(); i++) {
            if (maxval < imgMaskInter.getInt(i)) {
                maxval = imgMaskInter.getInt(i);
            }
        }

        int newval = -1;
        if (cmbLabel.getSelectedItem().equals(AUTO_INCREMENT)) {
            newval = nextMaskValue();
        } else {
            String tmp = (String)cmbLabel.getSelectedItem();
            try {
                newval = Integer.parseInt(tmp.split(" - ")[0]);
            } catch (NumberFormatException exc) {
                exc.printStackTrace();
            }
        }

        if (newval != maxval) {
            for(i=0; i<imgMaskInter.getSize(); i++) {
                if (maxval == imgMaskInter.getInt(i)) {
                    imgMaskInter.set(i, newval);
                }
            }
        }

        // show new mask
        showMask();
	}

	/**
	 * 
	 */
	protected void modifyLabel() {
        if (!checkMaskValid()) {
            return;
        }

        if (cmbLabel.getSelectedItem().equals(AUTO_INCREMENT)) {
            int    nxt = nextMaskValue();
            addLabel(nxt, txtLabelName.getText(), false);
            txtLabelValue.setText("" + (nxt+1));
            txtLabelName.setText("");
        } else {
            try {
                txtLabelValue.commitEdit();
                int    val    = ((Number)txtLabelValue.getValue()).intValue();
                String obj    = (String)cmbLabel.getSelectedItem();
                int    oldval = Integer.parseInt(obj.split(" - ")[0]);
                if (oldval != val) {
                    int      i;
                    /*
                    for(i=1; i<cnt; i++) {
                        tmp   = (String)cmbLabel.getItemAt(i);
                        if (!tmp.equals(obj)) {
                            parts = tmp.split(" - ");
                            try {
                                t = Integer.parseInt(parts[0]);
                                if (t == val) {
                                    String msg = "Merge mask '" + cmbLabel.getItemAt(i) + "' and '" + obj + "' to '" + val + " - " + txtLabelName.getText() + "'?";
                                    if (JOptionPane.showConfirmDialog(this, msg, "Mask Merge?",
                                                                      JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                                        txtLabelValue.setText("" + oldval);
                                        return;
                                    } else {
                                        cmbLabel.removeItem(tmp);
                                        i--;
                                        cnt--;
                                    }
                                }
                            } catch (Exception exc) {
                                exc.printStackTrace();
                            }
                        }
                    }
                    */

                    addLabel(val, txtLabelName.getText(), true);
                    cmbLabel.removeItem(obj);

                    // update the mask
                    for(i=0; i<imgMaskFinal.getSize(); i++) {
                        if (imgMaskFinal.getInt(i) == oldval)
                            imgMaskFinal.set(i, val);
                    }
                    System.arraycopy(imgMaskFinal.getData(), 0,
                                     imgMaskInter.getData(), 0,
                                     (int)imgMaskFinal.getSize());  
                } else {
                    // same val, no need to worry about reuse changing
                    addLabel(val, txtLabelName.getText(), true);
                }
            } catch (Exception exc) {
                exc.printStackTrace();
                txtLabelValue.setText("0");
            }
        }

        segmentImage();
        showMask();
	}

	/**
	 * 
	 */
	protected void selectLabel() {
        if (cmbLabel.getSelectedItem() == null) {
            return;
        }
        if (cmbLabel.getSelectedItem().equals(AUTO_INCREMENT)) {
            txtLabelValue.setText("" + nextMaskValue());
            txtLabelValue.setEditable(false);
            txtLabelName.setText("");
        } else {
            String   str  = (String)cmbLabel.getSelectedItem();
            String[] part = str.split(" - ");
            txtLabelValue.setText(part[0]);
            txtLabelValue.setEditable(true);
            if (part.length == 2) {
                txtLabelName.setText(part[1]);
            } else {
                txtLabelName.setText("");
            }
        }
        segmentImage();
	}

	/**
	 * @param i
	 * @param auto_increment2
	 * @param b
	 */
	private void addLabel(int maskval, String label, boolean select) {
	      String lbl;

	        synchronized(getTreeLock()) {
	            // special case for AUTO_INCREMENT, always at the top.
	            if (label.equals(AUTO_INCREMENT)) {
	                if (cmbLabel.getItemCount() == 0) {
	                    cmbLabel.addItem(AUTO_INCREMENT);
	                } else if (!cmbLabel.getItemAt(0).equals(AUTO_INCREMENT)) {
	                    cmbLabel.insertItemAt(AUTO_INCREMENT, 0);
	                }
	                lbl = AUTO_INCREMENT;

	            } else {
	                int    tmpval;
	                String tmp;

	                int    cnt = cmbLabel.getItemCount();
	                for(int i=1; i<cnt; i++) {
	                    tmp = (String)cmbLabel.getItemAt(i);
	                    try {
	                        tmpval = Integer.parseInt(tmp.split(" - ")[0]);
	                    } catch (NumberFormatException exc) {
	                        exc.printStackTrace();
	                        return;
	                    }
	                    if (tmpval == maskval) {
	                    	logger.error(maskval + " already exists as index, removing!");
	                        cmbLabel.removeItem(tmp);
	                        i--;
	                        cnt--;
	                    }
	                }
	                lbl = maskval + " - " + label;
	                cmbLabel.addItem(lbl);
	            }

	            if (select) {
	                cmbLabel.setSelectedItem(lbl);
	            }
	        }
	}

	/**
	 * 
	 */
	protected void applyPointToMask() {
        if (cmbLabel.getSelectedItem().equals(AUTO_INCREMENT)) {
            addLabel(nextMaskValue(), "unlabeled", false);
        }

        System.arraycopy(imgMaskInter.getData(), 0, imgMaskFinal.getData(), 0,
                	     (int)imgMaskInter.getSize());  

        Hashtable ht = new Hashtable();
        for(int i=0; i<imgMaskFinal.getSize(); i++) {
            Integer tmp = new Integer(imgMaskFinal.getInt(i));
            if (ht.get(tmp) == null) {
                ht.put(tmp, new int[1]);
            }
            ((int[])ht.get(tmp))[0]++;
        }

        txtMessage.setText("ImageMask\tCount\n");
        for(Enumeration keys = ht.keys(); keys.hasMoreElements(); ) {
            Integer key = (Integer)keys.nextElement();
            txtMessage.append(getLabel(key.intValue()) + "\t" + ((int[])ht.get(key))[0] + "\n");
        }

        impoint.x = -1;
        impoint.y = -1;
        showMask();
	}

	/**
	 * 
	 */
	protected void selectMaskPoint() {
        if (!checkMaskValid() || (frmMask == null)) {
            return;
        }

   
        //impoint.x = imagepanel.Display2SourceCoordRow(imagepanel.GetRowReleased()); //   if i call getLocationClicked, what does it return, do I still need x and y points?
        //impoint.y = imagepanel.Display2SourceCoordCol(imagepanel.GetColReleased());
        Point pts = frmMask.getImagePanel().getImageLocationClicked();
        impoint.x = pts.y;
        impoint.y = pts.x;
        
        txtMessage.append("Selected Mask Pts (row=" + impoint.x + ", col=" + impoint.y + ")\n");

        segmentImage();
	}

	/**
	 * 
	 */
	protected void selectOriginalPoint() {
        if (!checkMaskValid() || (imagepanel == null)) {
            return;
        }

       // ImagePanel imgpanel = frmMain.GetImagePanel();
        Point pts = imagepanel.getImageLocationClicked();
        impoint.x = pts.y;
        impoint.y = pts.x;
        
        txtMessage.append("Selected Original Pts (row=" + impoint.x + ", " + impoint.y + ")\n");
        logger.info("INFO: col?= " + pts.x + "row= " + pts.y + txtMessage);

        segmentImage();
	}
	
    protected void findMinMax(ImageObject img){
        //sanity check
        if(img == null){
        	logger.error("ERROR: internal image object is null");
          return;
        }

        if(img.getTypeString().equalsIgnoreCase("BYTE")){
            minmax[0][0] = 0;
            minmax[0][1] = Math.sqrt(img.getNumBands())*255.0;
            minmax[1][0] = 0;
            minmax[1][1] = 255;

        } else if(img.getTypeString().equalsIgnoreCase("SHORT") || img.getTypeString().equalsIgnoreCase("USHORT")){
            minmax[0][0] = 0;
            minmax[0][1] = Math.sqrt(img.getNumBands())*65536.0;
            minmax[1][0] = 0;
            minmax[1][1] = 65536;
        } else if(img.getTypeString().equalsIgnoreCase("INT")){
        minmax[1][0] = MAX_INT;
        minmax[1][1] = MIN_INT;
        for(int i=0;i<img.getSize();i++){
            if(minmax[1][0] > img.getInt(i) )
                minmax[1][0] = img.getInt(i);
            if(minmax[1][1] < img.getInt(i))
                minmax[1][1] = img.getInt(i);
        }
        minmax[0][0] = 0;
        minmax[0][1] = Math.sqrt(img.getNumBands())*(minmax[1][1] - minmax[1][0]);
        minmax[1][0]--;
        minmax[1][1]++;

        
        }else if(img.getTypeString().equalsIgnoreCase("FLOAT") || img.getTypeString().equalsIgnoreCase("LONG")){
          minmax[1][0] = MAX_FLOAT;
          minmax[1][1] = MIN_FLOAT;
          for(int i=0;i<img.getSize();i++){
              if(minmax[1][0] > img.getFloat(i) )
                  minmax[1][0] = img.getFloat(i);
              if(minmax[1][1] < img.getFloat(i))
                  minmax[1][1] = img.getFloat(i);
          }
          minmax[0][0] = 0;
          minmax[0][1] = Math.sqrt(img.getNumBands())*(minmax[1][1] - minmax[1][0]);
          minmax[1][0]--;
          minmax[1][1]++;

        } if(img.getTypeString().equalsIgnoreCase("DOUBLE")){
            minmax[1][0] = MAX_DOUBLE;
            minmax[1][1] = MIN_DOUBLE;
            for(int i=0;i<img.getSize();i++){
                if(minmax[1][0] > img.getDouble(i) )
                    minmax[1][0] = img.getDouble(i);
                if(minmax[1][1] < img.getDouble(i))
                    minmax[1][1] = img.getDouble(i);
            }
            minmax[0][0] = 0;
            minmax[0][1] = Math.sqrt(img.getNumBands())*(minmax[1][1] - minmax[1][0]);
            minmax[1][0]--;
            minmax[1][1]++;

        }
    }
    
    protected boolean checkMaskValid() {
        ImageObject original = imagepanel.getImageObject();

        if (imgMaskFinal == null) {
            return false;
        }

        if ((imgMaskFinal.getNumRows() != original.getNumRows()) ||
            (imgMaskFinal.getNumCols() != original.getNumCols() )) {
        	logger.debug("WARNING: a new image was loaded. Masks will be reset");
          reset();
          return false;
        }

        return true;
    }
    
    protected int currentMaskValue() {
        String str = (String)cmbLabel.getSelectedItem();

        if (str.equals(AUTO_INCREMENT)) {
            return nextMaskValue();
        } else {
            return Integer.parseInt(str.split(" - ")[0]);
        }
    }
    
    /**
     * Returns the next mask value to use. Will check the combobox and the
     * final mask for any mask values used. Will use the next biggest value,
     * not an unused value!
     * @return mext mask value to use.
     */
    protected int nextMaskValue() {
        int      i, t;
        int      nxt = 1;
        String[] parts;
        int      cnt = cmbLabel.getItemCount();

        // search labels for largest value
        for(i=1; i<cnt; i++) {
            parts = ((String)cmbLabel.getItemAt(i)).split(" - ");
            try {
                t = Integer.parseInt(parts[0]);
                if (t >= nxt)
                    nxt = t + 1;
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }

        if (imgMaskFinal != null) {
            // search mask for largest value
            for(i=0; i<imgMaskFinal.getSize(); i++) {
                if (imgMaskFinal.getInt(i) >= nxt)
                    nxt = imgMaskFinal.getInt(i) + 1;
            }
        }

        return nxt;
    }
    
    protected String getLabel(int maskval) {
        if (maskval == 0) {
            return AUTO_INCREMENT;
        } else {
            for(int i=1; i<cmbLabel.getItemCount(); i++) {
                String tmp     = (String)cmbLabel.getItemAt(i);
                String parts[] = tmp.split(" - ");

                try {
                    if (Integer.parseInt(parts[0]) == maskval) {
                        return parts[1];
                    }
                } catch (NumberFormatException exc) {
                    exc.printStackTrace();
                }
            }

            return "???";
        }
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
        JMenu segment = new JMenu("Segment");
        segment.add(new JMenuItem(new AbstractAction("Segment 2D Super") {
            public void actionPerformed(ActionEvent e) {
                if (!isVisible()) {
                    Window win = SwingUtilities.getWindowAncestor(imagepanel);
                    setLocationRelativeTo(win);
                    setVisible(true);
                    reset();
                }
                toFront();
            }
        }));
        tools.add(segment);

        return new JMenuItem[]{tools};
    }

    public void imageUpdated(ImageUpdateEvent event) {
    }
    
    public URL getHelp(String menu) {
        URL url = null;
        
        if (url == null) {
            String file = menu.toLowerCase().replaceAll("[\\s\\?\\*]", "") + ".html"; 
            url = this.getClass().getResource("help/" + file);
        }
        
        return url;
    } 
    
}
