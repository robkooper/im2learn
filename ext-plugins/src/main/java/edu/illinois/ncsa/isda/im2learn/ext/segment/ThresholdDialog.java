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
/*
 * ThresholdDialog.java
 *
 */



/**
 *
 * @author Peter Bajcsy
 * @version 1.0
 */

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import java.io.IOException;
import java.net.URL;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.datatype.*;
import edu.illinois.ncsa.isda.im2learn.core.display.*;
import edu.illinois.ncsa.isda.im2learn.core.io.FileChooser;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageLoader;
import edu.illinois.ncsa.isda.im2learn.ext.misc.ImageFrame;

/**
 * 
 *  <p>   
	The class Threshold is a tool for two-class clustering problems using
	Euclidean distance, a hypercube or a hyper plane for separating clusters in
	high-dimensional space. 
	</p>
	<p>
	<B>Description: </B> The class operates in three distinct modes that are set
	in the ThreshType choice list. In these three modes, points in
	high-dimensional space are classified based on (1) their Euclidean distance
	from the origin (a scalar value), (2) a hypercube (a vector) or (3) a hyper
	plane (two vectors). 
	</p>
	<img src="help/tresholddialog.jpg"></br>

	<p>
	<B>Setup: </B> In the "Distance (scalar)" mode, the scalar value is set
	either in the edit box or by moving the slider below the edit box denoted as
	Value(Point). The value is also shown in the text area together with the
	outcome of the thresholding operation. The outcome of the thresholding
	operation is the number of points below the threshold denoted as "BlackCount"
	and above the threshold denoted as "WhiteCount". <BR>
	In the "Box (vector)" mode, it is possible to set the upper corner of a
	hypercube by selecting one of the point dimensions in the choice list labeled
	as "ThreshPoint". The coordinate can then be modified either in the edit box
	or by moving the slider. The upper corner of a box (hypercube) is defined as
	the point that encloses the maxima of all points denoted as "Black". The
	lower corner is always set to the minimum coordinate in each dimension. <BR>
	The last option "Plane (2 vectors)" of the "ThreshType" choice list defines a
	hyper plane by setting one point in the plane (ValuePoint) and one point as
	the tip of the normal to the plane (ThreshVecNormal). The settings can be
	modified either in the edit boxes or by moving the sliders. 
	</p>
	<p>
	<B>Run: </B> The text area reports input parameters (a scalar, one vector or
	two vectors) and output parameters (BlackCount and WhiteCount). It is
	possible to view the thresholded images by clicking "Show" and save them by
	clicking "Save". Any changes in the input parameters will be automatically
	updated in the frame showing the thresholded images. Although the default
	values for each mode correspond to statistical thresholds (e.g., the sample
	mean of the image), further input parameter tuning is facilitated by on-line
	visualization of the thresholded image with the "Show" button. 
	</p>

	<img src="help/tresholddialog1.jpg" width="415"
	height="267"></br></br>

	<img src="help/tresholddialog2.jpg" width="425"
	height="272">

	<p>
	<B>Release notes: </B> <BR>
	Java sliders support only integer data type therefore any double precision
	thresholds must be entered via edit boxes. The value that is shown in the
	edit boxes will be the actual value used for thresholding.
	</p>
  
 * 
 * 
 * 
 */

public class ThresholdDialog extends Im2LearnFrame implements Im2LearnMenu {

  public ImagePanel imagepanel;
  public JPanel pnl;
  public JPanel pnlType;
  public JPanel pnlField;
  public JPanel pnlSlider;
  public JPanel pnlBtns;

  public JButton btnDone = new JButton("Done");
  public JButton btnShow = new JButton("show");
  public JButton btnSave = new JButton("Save");

  public JLabel JLabelThreshType = new JLabel("ThreshType");

  public JLabel JLabelThreshBand = new JLabel("ThreshPoint");
  public JLabel JLabelThreshBand1 = new JLabel("ThreshVectNormal");

  public JLabel JLabelThreshValue = new JLabel("Value(Point)");
  public JLabel JLabelThreshValue1 = new JLabel("Value(VectNormal)");
  
  public JLabel lblThreshMode = new JLabel("Choose Thresh Mode: ");
  

  public JComboBox cbxThreshMode;//select less than; greater than; in-between; less than lower and greater than upper
  public JComboBox cbxThreshBand;
  public JComboBox cbxThreshBand1;
  public JComboBox cbxThreshType; 
  
  public JLabel lblLowerThresh = new JLabel("Lower Thresh");
  public JLabel lblUpperThresh = new JLabel("Upper Thresh");
  
  
  public JPanel pnlThreshValueLower = new JPanel(new CardLayout());
  public JPanel pnlThreshValueUpper = new JPanel(new CardLayout());

  public JTextField fieldThreshValue = new JTextField("35", 4);
  public JTextField fieldThreshValue1 = new JTextField("35", 4);

  public JSlider slider = new JSlider(0,200,35);
  public JSlider slider1 = new JSlider(0,200,35);

  public TextArea fieldThreshVector = new TextArea(5,40);
 
  public int threshDim = 1;
  public int threshBand = 0;
  public int threshBand1 = 0;
  public int numBands = 0;
  public double threshValLower; // constant
  public double threshValUpper;
  public double minVal, maxVal;

  public ImageObject threshValuesLower = null;//new ImageObject();
  public ImageObject threshValuesUpper = null;
  public double [] minValues;// box
  public double [] maxValues;// box

  public ImageObject threshPlane = null;//new ImageObject();//plane point
  public ImageObject threshNormal = null;//new ImageObject();//normal plane
  public double [] minValues1;// plane
  public double [] maxValues1;// plane
  
  public Threshold myThresh = new Threshold();
  public ImageFrame myShowImage=null;

  public ImageObject imgObject;
  public static Log logger = LogFactory.getLog(ThresholdDialog.class);
  
  public ImagePanel ip = new ImagePanel();//this panel is only for MaskCreate pane in geolearn
  public String textForGeolearn = "";//this panel is only for MaskCreate pane in geolearn
  public JPanel geoleanrpnlip = new JPanel(new BorderLayout());
  public JTextArea geolearntaCounts = new JTextArea();
  public boolean geolearn = false;

  public ThresholdDialog() {
    super("Image Threshold");

    myShowImage = new ImageFrame("Thresholded Image");

    createUI();
//    showing();
  }
  
  public void showing(){

	    if(imagepanel.getImageObject() == null){
	      System.err.println("Error: no image to process !");
	      return;
	    }
	    imgObject = imagepanel.getImageObject();
	    myShowImage = new ImageFrame("Thresholded Image");
	    
	    imagepanel.setAutozoom(true);
		ip.setAutozoom(true);

	    try{
	      threshValuesLower = threshValuesLower.createImage(1,1,imgObject.getNumBands(), "DOUBLE");
	      threshValuesUpper = threshValuesUpper.createImage(1,1,imgObject.getNumBands(), "DOUBLE");
	      threshPlane = threshPlane.createImage(1,1,imgObject.getNumBands(), "DOUBLE");
	      threshNormal = threshNormal.createImage(1,1,imgObject.getNumBands(), "DOUBLE");
	    } catch(Exception e){
	      System.err.println("Error: Exception error while allocating memory !");
	      return;
	    }
	    minValues = new double[imgObject.getNumBands()];
	    maxValues = new double[imgObject.getNumBands()];

	    minValues1 = new double[imgObject.getNumBands()];
	    maxValues1 = new double[imgObject.getNumBands()];


	    myThresh.initMemory(imgObject);
	    
	    cbxThreshType.removeAllItems();
	    cbxThreshType.addItem("Distance (scalar)");
	    
	    if(imagepanel.getImageObject().getNumBands()>1){
	    	cbxThreshType.addItem("Box (vector)");
	    	cbxThreshType.addItem("Plane (2 vectors)");
	    	
	    	cbxThreshBand.removeAllItems();
		    cbxThreshBand1.removeAllItems();
		    numBands=0;
		  	for(int i=0;i<imagepanel.getImageObject().getNumBands();i++){
		        cbxThreshBand.addItem(Integer.toString(i+1));
		        cbxThreshBand1.addItem(Integer.toString(i+1));
		        numBands++;
		  	}
		    cbxThreshBand.setSelectedIndex(0);
		    cbxThreshBand1.setSelectedIndex(0);
	    }

	    int i;
	    if(myThresh.FindMeanBands(imgObject)){
	      for(i=0;i<imgObject.getNumBands();i++){
	        threshValuesLower.set(i, myThresh.getHalfVolBand(i) );
	        threshValuesUpper.set(i, myThresh.getHalfVolBand(i) );
	        minValues[i] =  myThresh.getMinBand(i);
	        maxValues[i] =  myThresh.getMaxBand(i);

	        threshPlane.set(i, myThresh.getMeanBand(i) );//threshValues.imageDouble[i];
	        threshNormal.set(i, maxValues[i] );
	        minValues1[i] =  minValues[i];
	        maxValues1[i] =  maxValues[i];

	      }
	    }else{
	      System.out.println("Error: could not find mean, min and max values of bands");
	      for(i=0;i<imgObject.getNumBands();i++){
	        threshValuesLower.set(i,  50.0E1);
	        threshValuesUpper.set(i,  50.0E1);
	        minValues[i] = 0.0E1;
	        maxValues[i] = 100.0E1;

	        threshPlane.set(i,  50.0E1);
	        threshNormal.set(i, 0.0E1);
	        minValues1[i] = 0.0E1;
	        maxValues1[i] = 100.0E1;

	      }
	    }

	    threshDim =  1;
	    int val = 0;
	    threshValLower = myThresh.FindMeanDistance(imgObject);
	    threshValUpper = myThresh.FindMeanDistance(imgObject);
	    if(threshValLower == 0.0 || threshValUpper == 0.0){
	      System.out.println("Error: could not compute Mean Distance !");
	      return;
	    }
	    //myThresh.binarizeImage(imgObject, threshValLower, true,true);

	    fieldThreshValue.setText(Double.toString(threshValLower));
	    fieldThreshValue1.setText(Double.toString(threshValUpper));
	    String s = "Scalar: \nLower Thresh:";
	    s += Double.toString(threshValLower);

	    fieldThreshVector.setText(s);
	    geolearntaCounts.setText(s);
	    minVal = myThresh.getMinDistance();
	    slider.setMinimum((int)(minVal-1));
	    slider1.setMinimum((int)(minVal-1));
	    maxVal = myThresh.getMaxDistance();
	    slider.setMaximum((int)(maxVal+1));
	    slider1.setMaximum((int)(maxVal+1));
	    val = (int) (threshValLower+0.5);
	    slider.setValue(val);
	    //slider.setSize(200,25);
	    slider1.setValue(val);
	    //slider1.setSize(200,25);
	    fieldThreshValue.setEditable(true);
	    fieldThreshValue1.setEditable(true);
	    fieldThreshVector.setEditable(false);

	    //ThresholdDialog(myFrame,title, isModal);
	    cbxThreshType.setSelectedIndex(0);
	    cbxThreshMode.setSelectedIndex(0);

	  }

  /**
   * create image independent user interface
   */
  public void createUI() {
	  
	  
	String[] strgThreshType = {"Distance (scalar)"};
	cbxThreshType = new JComboBox(strgThreshType);
	cbxThreshType.setEditable(false);

    //String[] strgThreshMode = {"Greater than","Less than","In-between","Less than lower && greater than upper"};
    String[] strgThreshMode = {"Greater than","Less than","In-between","Outside"};
    cbxThreshMode = new JComboBox(strgThreshMode);
    cbxThreshMode.setEditable(false);
    
  	cbxThreshBand = new JComboBox();
  	cbxThreshBand1 = new JComboBox();
    cbxThreshBand.setEditable(false);
    cbxThreshBand1.setEditable(false);
    
    pnlType = new JPanel(new GridLayout(2,1));

    pnlThreshValueLower.add(lblLowerThresh,"Lower Threshold");
    pnlThreshValueLower.add(JLabelThreshValue,"Value(Point)");
    
    pnlThreshValueUpper.add(lblUpperThresh,"Upper Threshold");
    pnlThreshValueUpper.add(JLabelThreshValue1,"Value(VectNormal)");

    pnlSlider = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1;
    c.gridwidth = 1;
    c.insets = new Insets(4,4,4,4);
    c.gridx = 0;
    c.gridy = 0;
    pnlSlider.add(JLabelThreshType,c);
    c.gridx = 1;
    c.gridy = 0;
    pnlSlider.add(lblThreshMode,c);
    c.gridx = 0;
    c.gridy = 1;
    pnlSlider.add(cbxThreshType,c);
    c.gridx = 1;
    c.gridy = 1;
    pnlSlider.add(cbxThreshMode,c);
    c.gridx = 0;
    c.gridy = 2;
    pnlSlider.add(JLabelThreshBand,c);
    c.gridx = 1;
    c.gridy = 2;
    pnlSlider.add(JLabelThreshBand1,c);
    c.gridx = 0;
    c.gridy = 3;
    pnlSlider.add(cbxThreshBand,c);
    c.gridx = 1;
    c.gridy = 3;
    pnlSlider.add(cbxThreshBand1,c);
    c.gridx = 0;
    c.gridy = 4;
    pnlSlider.add(pnlThreshValueLower,c);
    c.gridx = 1;
    c.gridy = 4;
    pnlSlider.add(pnlThreshValueUpper,c);
    c.gridx = 0;
    c.gridy = 5;
    pnlSlider.add(fieldThreshValue,c);
    c.gridx = 1;
    c.gridy = 5;
    pnlSlider.add(fieldThreshValue1,c);
    c.gridx = 0;
    c.gridy = 6;
    pnlSlider.add(slider,c);
    c.gridx = 1;
    c.gridy = 6;
    pnlSlider.add(slider1,c);

    
    pnlField = new JPanel();
    pnlField.setLayout(new GridLayout(1,1) );
    pnlField.add(fieldThreshVector);
    
    pnl = new JPanel();
    BoxLayout boxLayoutPnl = new BoxLayout(pnl, BoxLayout.Y_AXIS);
    pnl.setLayout(boxLayoutPnl);
    pnl.add(pnlType);
    pnl.add(pnlSlider);
    pnl.add(pnlField);
    
    pnlBtns = new JPanel(new FlowLayout());
    pnlBtns.add(btnShow);
    pnlBtns.add(btnSave);
    pnlBtns.add(btnDone);

    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(pnl, BorderLayout.CENTER);
    getContentPane().add(pnlBtns, BorderLayout.SOUTH);
    
    pack();
    
    JLabelThreshBand.setVisible(false);
    JLabelThreshBand1.setVisible(false);
    cbxThreshBand.setVisible(false);
    cbxThreshBand1.setVisible(false);
    lblThreshMode.setVisible(true);
    cbxThreshMode.setVisible(true);
    
    fieldThreshValue1.setEnabled(false);
	slider1.setEnabled(false);
	
    cbxThreshType.setAction(new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
        	setThreshTypeAction();
        }
     });

    cbxThreshMode.setAction(new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            updateThreshMode();
            setText();
            }
     });

    cbxThreshBand.setAction(new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
        	setThreshBandAction();
        }
    });

    cbxThreshBand1.setAction(new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
        	setThreshBand1Action();
        }
    });

    slider.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ce) {
          JSlider src = (JSlider)ce.getSource();
          if(!src.getValueIsAdjusting()) {
        	  doThresh("slider");
          }
        }
    });
    
    slider1.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ce) {
          JSlider src = (JSlider)ce.getSource();
          if(!src.getValueIsAdjusting()) {
        	  doThresh("slider1");}
        }
    });
    
    fieldThreshValue.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent event) {
        	String s1 = fieldThreshValue.getText();
            double temp;
            try{
             temp = Double.parseDouble(s1);
            }
            catch(NumberFormatException e){
             System.out.println("Error: Enter a number ! ");
             return;
            }
        	doThresh("textField");
        }
    });


    fieldThreshValue1.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent event) {
        	String s1 = fieldThreshValue1.getText();
            double temp;
            try{
             temp = Double.parseDouble(s1);
            }
            catch(NumberFormatException e){
             System.out.println("Error: Enter a number ! ");
             return;
            }
        	doThresh("textField1");
        }
    });
    
    btnShow.setAction(new AbstractAction("Show"){
    	public void actionPerformed(ActionEvent e){
    		myShowImage.setVisible(true);
    		setText();
    	}
    });
    
    btnSave.setAction(new AbstractAction("Save"){
    	public void actionPerformed(ActionEvent e){
    		System.out.println("test inside saveActivated =");
    	      FileChooser fc = new FileChooser();
    	       fc.setTitle("Save Resulting Image");
    	       try {
    	           String filename = fc.showSaveDialog();
    	           if (filename != null) {
    	               ImageLoader.writeImage(filename, myThresh.getImageObject());
    	           }
    	       } catch (IOException exc) {
    	           logger.error("Error saving result.", exc);
    	       }
    	}
    });
    
    btnDone.setAction(new AbstractAction("Done"){
    	public void actionPerformed(ActionEvent e){
    		dispose();
    	    myShowImage.dispose();
    	}
    });
    
    pack();
    setSize(getPreferredSize());
    
//  forgeolearn:
	  geolearntaCounts.setEditable(false);
	  geoleanrpnlip.add(new JScrollPane(ip),BorderLayout.CENTER);
	  geoleanrpnlip.add(geolearntaCounts,BorderLayout.SOUTH);
  }
  
  public void closing (){
    dispose();
    if(myShowImage.isVisible()){
      myShowImage.setVisible(false);
      myShowImage.dispose();
    }
  }
  
  public void setThreshTypeAction(){
  	if( imgObject == null ){
          logger.error("ERROR: image object is null");
          return;
        }
  	CardLayout clThreshLower = (CardLayout)(pnlThreshValueLower.getLayout());
      CardLayout clThreshUpper = (CardLayout)(pnlThreshValueUpper.getLayout());
          
          if(cbxThreshType.getSelectedIndex()==0){
              clThreshLower.show(pnlThreshValueLower,"Lower Threshold");
              clThreshUpper.show(pnlThreshValueUpper,"Upper Threshold");
              JLabelThreshBand.setVisible(false);
              JLabelThreshBand1.setVisible(false);
              cbxThreshBand.setVisible(false);
              cbxThreshBand1.setVisible(false);
              lblThreshMode.setVisible(true);
              cbxThreshMode.setVisible(true);
              slider.setMinimum((int)(minVal-1));
              slider.setMaximum((int)(maxVal+1));
              slider.setValue((int)(threshValLower+0.5));
              slider1.setMinimum((int)(minVal-1));
              slider1.setMaximum((int)(maxVal+1));
              slider1.setValue((int)(threshValUpper+0.5));
              fieldThreshValue.setText(Double.toString(threshValLower));
              fieldThreshValue1.setText(Double.toString(threshValLower));
              threshDim =  1;
              updateThreshMode();
              setText();
              
          }else if(cbxThreshType.getSelectedIndex()==1 && imgObject.getNumBands() > 1){
          	clThreshLower.show(pnlThreshValueLower,"Lower Threshold");
              clThreshUpper.show(pnlThreshValueUpper,"Upper Threshold");
              JLabelThreshBand1.setVisible(false);
              cbxThreshBand1.setVisible(false);
              JLabelThreshBand.setVisible(true);
              cbxThreshBand.setVisible(true);
              lblThreshMode.setVisible(true);
              cbxThreshMode.setVisible(true);
              
              slider.setMinimum((int)minValues[threshBand]-1);
              slider.setMaximum((int)maxValues[threshBand]+1);
              slider.setValue((int)(threshValuesLower.getDouble(threshBand)+0.5) );
              slider1.setMinimum((int)minValues[threshBand]-1);
              slider1.setMaximum((int)maxValues[threshBand]+1);
              slider1.setValue((int)(threshValuesUpper.getDouble(threshBand)+0.5) );
              fieldThreshValue.setText(Double.toString(threshValuesLower.getDouble(threshBand)));
              fieldThreshValue1.setText(Double.toString(threshValuesUpper.getDouble(threshBand)));
              threshDim =  imagepanel.getImageObject().getNumBands();
              updateThreshMode();
              setText();

          }else if(cbxThreshType.getSelectedIndex()==2 && imgObject.getNumBands() > 1){
          	clThreshLower.show(pnlThreshValueLower,"Value(Point)");
              clThreshUpper.show(pnlThreshValueUpper,"Value(VectNormal)");
              JLabelThreshBand.setVisible(true);
              JLabelThreshBand1.setVisible(true);
              cbxThreshBand.setVisible(true);
              cbxThreshBand1.setVisible(true);
              lblThreshMode.setVisible(false);
              cbxThreshMode.setVisible(false);
              fieldThreshValue1.setEditable(true);
              slider1.setEnabled(true);
              fieldThreshValue1.setEnabled(true);
              threshDim =  imgObject.getNumBands() << 1;
              fieldThreshValue.setText(Double.toString(threshPlane.getDouble(threshBand) ));
              slider.setMinimum((int)minValues[threshBand]-1);
              slider.setMaximum((int)maxValues[threshBand]+1);
              slider.setValue((int)(threshPlane.getDouble(threshBand)+0.5));
              fieldThreshValue1.setText(Double.toString(threshNormal.getDouble(threshBand1) ));
              slider1.setMinimum((int)minValues1[threshBand1]-1);
              slider1.setMaximum((int)maxValues1[threshBand1]+1);
              slider1.setValue((int)(threshNormal.getDouble(threshBand1)+0.5));
              setText();
          }
          
  }
  
  public void setThreshBandAction(){

  	threshBand = cbxThreshBand.getSelectedIndex();
  	slider.setMinimum((int)minValues[threshBand]-1);
      slider.setMaximum((int)maxValues[threshBand]+1);
      slider1.setMinimum((int)minValues[threshBand]-1);
      slider1.setMaximum((int)maxValues[threshBand]+1);
      if(cbxThreshType.getSelectedIndex() == 1){
         fieldThreshValue.setText(Double.toString(threshValuesLower.getDouble(threshBand) ));
         fieldThreshValue1.setText(Double.toString(threshValuesUpper.getDouble(threshBand) ));
         slider.setValue((int)(threshValuesLower.getDouble(threshBand)+0.5));
         slider1.setValue((int)(threshValuesUpper.getDouble(threshBand)+0.5));
      }
      if(cbxThreshType.getSelectedIndex() == 2) {
         fieldThreshValue.setText(Double.toString(threshPlane.getDouble(threshBand)));
         slider.setValue((int)(threshPlane.getDouble(threshBand)+0.5));
      }
  }
  
  public void setThreshBand1Action(){

  	threshBand1 = cbxThreshBand1.getSelectedIndex();
  	fieldThreshValue1.setText(Double.toString(threshNormal.getDouble(threshBand1) ));
  	slider1.setMinimum((int)minValues1[threshBand1]-1);
      slider1.setMaximum((int)maxValues1[threshBand1]+1);
      slider1.setValue((int)(threshNormal.getDouble(threshBand1) +0.5));
  
  }
  
  public void doThresh(String src){
	  if(cbxThreshType.getSelectedIndex() == 0) {
		  if(cbxThreshMode.getSelectedIndex() == 0 || cbxThreshMode.getSelectedIndex() == 1){
			  if(src.equals("slider")){
				  threshValLower = slider.getValue();
        		  fieldThreshValue.setText(Double.toString(threshValLower));
			  }else if(src.equals("slider1")){
				  threshValUpper = slider1.getValue();
        		  fieldThreshValue1.setText(Double.toString(threshValUpper));
			  }else if(src.equals("textField")){
				  slider.setValue((int)Double.parseDouble(fieldThreshValue.getText()));
				  threshValLower = slider.getValue();
			  }else if(src.equals("textField1")){
				  slider1.setValue((int)Double.parseDouble(fieldThreshValue1.getText()));
				  threshValUpper = slider1.getValue();
			  }
	      }else if(cbxThreshMode.getSelectedIndex() == 2 || cbxThreshMode.getSelectedIndex() == 3){
			  if(src.equals("slider")){
				  if(slider.getValue() > slider1.getValue()){
           		  slider.setValue(slider1.getValue());
           	      }
           	      fieldThreshValue.setText(Double.toString(slider.getValue()));
			  }else if(src.equals("slider1")){
				  if(slider.getValue() > slider1.getValue()){
	           		  slider1.setValue(slider.getValue());
	           	  }
	           	  fieldThreshValue1.setText(Double.toString(slider1.getValue()));
			  }else if(src.equals("textField")){
				  if(Double.parseDouble(fieldThreshValue.getText()) > slider1.getValue()){
           			fieldThreshValue.setText(Integer.toString(slider1.getValue()));
          		  }
         		   slider.setValue((int)Double.parseDouble(fieldThreshValue.getText()));
			  }else if(src.equals("textField1")){
				  if(slider.getValue() > Double.parseDouble(fieldThreshValue1.getText())){
           			fieldThreshValue1.setText(Integer.toString(slider.getValue()));
          		  }
         		   slider1.setValue((int)Double.parseDouble(fieldThreshValue1.getText()));
			  }
       	      threshValLower = slider.getValue();
    	      threshValUpper = slider1.getValue();
	     }
	  }else if(cbxThreshType.getSelectedIndex() == 1){
		  if(cbxThreshMode.getSelectedIndex() == 0 || cbxThreshMode.getSelectedIndex() == 1){
			  if(src.equals("slider")){
				  threshValuesLower.set(threshBand,slider.getValue());
        		  fieldThreshValue.setText(Double.toString(threshValLower));
			  }else if(src.equals("slider1")){
				  threshValuesUpper.set(threshBand,slider1.getValue());
        		  fieldThreshValue1.setText(Double.toString(threshValUpper));
			  }else if(src.equals("textField")){
				  slider.setValue((int)Double.parseDouble(fieldThreshValue.getText()));
				  threshValuesLower.set(threshBand,slider.getValue());
			  }else if(src.equals("textField1")){
				  slider1.setValue((int)Double.parseDouble(fieldThreshValue1.getText()));
				  threshValuesUpper.set(threshBand,slider1.getValue());
			  }
	      }else if(cbxThreshMode.getSelectedIndex() == 2 || cbxThreshMode.getSelectedIndex() == 3){
			  if(src.equals("slider")){
				  if(slider.getValue() > slider1.getValue()){
           		  slider.setValue(slider1.getValue());
           	      }
           	      fieldThreshValue.setText(Double.toString(slider.getValue()));
			  }else if(src.equals("slider1")){
				  if(slider.getValue() > slider1.getValue()){
	           		  slider1.setValue(slider.getValue());
	           	  }
	           	  fieldThreshValue1.setText(Double.toString(slider1.getValue()));
			  }else if(src.equals("textField")){
				  if(Double.parseDouble(fieldThreshValue.getText()) > slider1.getValue()){
           			fieldThreshValue.setText(Integer.toString(slider1.getValue()));
          		  }
         		   slider.setValue((int)Double.parseDouble(fieldThreshValue.getText()));
			  }else if(src.equals("textField1")){
				  if(slider.getValue() > Double.parseDouble(fieldThreshValue1.getText())){
           			fieldThreshValue1.setText(Integer.toString(slider.getValue()));
          		  }
         		   slider1.setValue((int)Double.parseDouble(fieldThreshValue1.getText()));
			  }
			  threshValuesLower.set(threshBand,slider.getValue());
			  threshValuesUpper.set(threshBand,slider1.getValue());
	     }
	  }else if(cbxThreshType.getSelectedIndex() == 2){
		  if(src.equals("slider")){
			  fieldThreshValue.setText(Double.toString(slider.getValue()));
			  threshPlane.set(threshBand,slider.getValue());
		  }else if(src.equals("slider1")){
			  fieldThreshValue1.setText(Double.toString(slider1.getValue()));
			  threshPlane.set(threshBand,slider1.getValue());
		  }else if(src.equals("textField")){
			  slider.setValue((int)Double.parseDouble(fieldThreshValue.getText()));
			  threshPlane.set(threshBand,Double.parseDouble(fieldThreshValue.getText()));
		  }else if(src.equals("textField1")){
			  slider1.setValue((int)Double.parseDouble(fieldThreshValue1.getText()));
			  threshPlane.set(threshBand,Double.parseDouble(fieldThreshValue1.getText()));
		  }
	  }
	  setText();
  }
  
  public void updateThreshMode(){
	  if(cbxThreshMode.getSelectedIndex()==0){
      	slider.setEnabled(true);
      	fieldThreshValue.setEnabled(true);
      	fieldThreshValue1.setEnabled(false);
      	slider1.setEnabled(false);
      }else if(cbxThreshMode.getSelectedIndex()==1){
      	slider1.setEnabled(true);
      	fieldThreshValue1.setEnabled(true);
      	fieldThreshValue.setEnabled(false);
      	slider.setEnabled(false);
      }else if(cbxThreshMode.getSelectedIndex()==2 || cbxThreshMode.getSelectedIndex()==3){
      	slider.setEnabled(true);
      	fieldThreshValue.setEnabled(true);
      	fieldThreshValue1.setEnabled(true);
      	slider1.setEnabled(true);
      }
  }

  public void setText(){
	  String s = "";
	  if(cbxThreshType.getSelectedIndex() == 0){
		  if(cbxThreshMode.getSelectedIndex() == 0){
			  s = "Scalar: \nLower Thresh: ";
              s += Double.toString(threshValLower);
              if(myShowImage.isVisible() || geolearn ){
            	  if(myThresh.binarizeImage(imgObject,threshValLower,true,true) ){
            		  ip.setImageObject(myThresh.getImageObject());
            		  myShowImage.setImageObject(myThresh.getImageObject() );
                      s += "\nBlackCount=";
                      s += Integer.toString(myThresh.getBlackCount());
                      s += " WhiteCount=";
                      s += Integer.toString(myThresh.getWhiteCount());
                      }
            	  }
              fieldThreshVector.setText(s);
              geolearntaCounts.setText(s);
		  }else if(cbxThreshMode.getSelectedIndex() == 1){
			  s = "Scalar: \nUpper Thresh: ";
              s += Double.toString(threshValUpper);
              if(myShowImage.isVisible() || geolearn ){
            	  if(myThresh.binarizeImage(imgObject,threshValUpper,false,true) ){
            		  ip.setImageObject(myThresh.getImageObject());
            		  myShowImage.setImageObject(myThresh.getImageObject() );
                      s += "\nBlackCount=";
                      s += Integer.toString(myThresh.getBlackCount());
                      s += " WhiteCount=";
                      s += Integer.toString(myThresh.getWhiteCount());
                      }
            	  }
              fieldThreshVector.setText(s);
              geolearntaCounts.setText(s);
		  }else if(cbxThreshMode.getSelectedIndex() == 2){
			  s = "Scalar: \nLower Thresh: ";
              s += Double.toString(threshValLower);
              s += "\nUpper Thresh: ";
              s += Double.toString(threshValUpper);
              if(myShowImage.isVisible() || geolearn ){
                  if(myThresh.binarizeImage(imgObject,threshValLower,threshValUpper,true,true) ){
                	ip.setImageObject(myThresh.getImageObject());
                    myShowImage.setImageObject(myThresh.getImageObject() );
                    s += "\nBlackCount=";
                    s += Integer.toString(myThresh.getBlackCount());
                    s += " WhiteCount=";
                    s += Integer.toString(myThresh.getWhiteCount());
                  }
               }
              fieldThreshVector.setText(s);
              geolearntaCounts.setText(s);
		  }else if(cbxThreshMode.getSelectedIndex() == 3){
			  s = "Scalar: \nLower Thresh: ";
              s += Double.toString(threshValLower);
              s += "\nUpper Thresh: ";
              s += Double.toString(threshValUpper);
              if(myShowImage.isVisible() || geolearn ){
                  if(myThresh.binarizeImage(imgObject,threshValLower,threshValUpper,true,false) ){
                	ip.setImageObject(myThresh.getImageObject());
                    myShowImage.setImageObject(myThresh.getImageObject() );
                    s += "\nBlackCount=";
                    s += Integer.toString(myThresh.getBlackCount());
                    s += " WhiteCount=";
                    s += Integer.toString(myThresh.getWhiteCount());
                  }
               }
              fieldThreshVector.setText(s);
              geolearntaCounts.setText(s);
		  }
	  }else if(cbxThreshType.getSelectedIndex() == 1){
          if(cbxThreshMode.getSelectedIndex() == 0){
        	  s = "Point: \nLower Thresh: ";
              for(int k = 0; k< threshDim; k++){
                  s += Double.toString(threshValuesLower.getDouble(k) );
                  if(k != threshDim-1)
                    s += ", ";
                }
              
              if(myShowImage.isVisible() || geolearn ){
                  if(myThresh.binarizeImage(imgObject,threshValuesLower,true,true) ){
                    myShowImage.setImageObject(myThresh.getImageObject() );
                    ip.setImageObject(myThresh.getImageObject());
                    s += "\nBlackCount=";
                    s += Integer.toString(myThresh.getBlackCount());
                    s += " WhiteCount=";
                    s += Integer.toString(myThresh.getWhiteCount());
                  }
               }
              fieldThreshVector.setText(s);
              geolearntaCounts.setText(s);
		  }else if(cbxThreshMode.getSelectedIndex() == 1){
			  s = "Point: \nUpper Thresh: ";
              for(int k = 0; k< threshDim; k++){
                  s += Double.toString(threshValuesUpper.getDouble(k) );
                  if(k != threshDim-1)
                    s += ", ";
                }
              if(myShowImage.isVisible() || geolearn ){
                  if(myThresh.binarizeImage(imgObject,threshValuesUpper,false,true) ){
                    myShowImage.setImageObject(myThresh.getImageObject() );
                    ip.setImageObject(myThresh.getImageObject());
                    s += "\nBlackCount=";
                    s += Integer.toString(myThresh.getBlackCount());
                    s += " WhiteCount=";
                    s += Integer.toString(myThresh.getWhiteCount());
                  }
               }
              fieldThreshVector.setText(s);
              geolearntaCounts.setText(s);
		  }else if(cbxThreshMode.getSelectedIndex() == 2){
			  s = "Point: \nLower Thresh: ";
              for(int k = 0; k< threshDim; k++){
                  s += Double.toString(threshValuesLower.getDouble(k) );
                  if(k != threshDim-1)
                    s += ", ";
                }
              s += "\nUpper Thresh: ";
              for(int k = 0; k< threshDim; k++){
                  s += Double.toString(threshValuesUpper.getDouble(k) );
                  if(k != threshDim-1)
                    s += ", ";
                }
              if(myShowImage.isVisible() || geolearn ){
                  if(myThresh.binarizeImage(imgObject,threshValuesLower,threshValuesUpper,true,true) ){
                    myShowImage.setImageObject(myThresh.getImageObject() );
                    ip.setImageObject(myThresh.getImageObject());
                    s += "\nBlackCount=";
                    s += Integer.toString(myThresh.getBlackCount());
                    s += " WhiteCount=";
                    s += Integer.toString(myThresh.getWhiteCount());
                  }
               }
              fieldThreshVector.setText(s);
              geolearntaCounts.setText(s);
		  }else if(cbxThreshMode.getSelectedIndex() == 3){
			  s = "Point: \nLower Thresh: ";
              for(int k = 0; k< threshDim; k++){
                  s += Double.toString(threshValuesLower.getDouble(k) );
                  if(k != threshDim-1)
                    s += ", ";
                }
              s += "\nUpper Thresh: ";
              for(int k = 0; k< threshDim; k++){
                  s += Double.toString(threshValuesUpper.getDouble(k) );
                  if(k != threshDim-1)
                    s += ", ";
                }
              if(myShowImage.isVisible() || geolearn ){
                  if(myThresh.binarizeImage(imgObject,threshValuesLower,threshValuesUpper,true,false) ){
                    myShowImage.setImageObject(myThresh.getImageObject() );
                    ip.setImageObject(myThresh.getImageObject());
                    s += "\nBlackCount=";
                    s += Integer.toString(myThresh.getBlackCount());
                    s += " WhiteCount=";
                    s += Integer.toString(myThresh.getWhiteCount());
                  }
               }
              fieldThreshVector.setText(s);
              geolearntaCounts.setText(s);
		  }
	  }else if(cbxThreshType.getSelectedIndex() == 2){
		  s = "Point: ";
          for(int k = 0; k< imgObject.getNumBands(); k++){
           s += Double.toString(threshPlane.getDouble(k) );
           if(k != imgObject.getNumBands() - 1)
             s += ", ";
          }
          s += "\nNormal: ";
          for(int k = 0; k< imgObject.getNumBands(); k++){
              s += Double.toString(threshNormal.getDouble(k) );
              if(k != imgObject.getNumBands() - 1)
                s += ", ";
          }
          if(myShowImage.isVisible() || geolearn ){
             if(myThresh.binarizeImage(imgObject,threshPlane,threshNormal, true) ){
               myShowImage.setImageObject(myThresh.getImageObject() );
               ip.setImageObject(myThresh.getImageObject());
            }
          }
          if(myShowImage.isVisible() || geolearn ){
              s += "\nBlackCount=";
              s += Integer.toString(myThresh.getBlackCount());
              s += " WhiteCount=";
              s += Integer.toString(myThresh.getWhiteCount());
            }
          fieldThreshVector.setText(s);
          geolearntaCounts.setText(s);
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
        JMenu thres = new JMenu("Threshold");
        JMenuItem threshold = new JMenuItem(new AbstractAction("Band Threshold") {
            public void actionPerformed(ActionEvent e) {
                if (!isVisible()) {
                    Window win = SwingUtilities.getWindowAncestor(imagepanel);
                    setLocationRelativeTo(win);
                    setVisible(true);
                }
                toFront();
            }
        });
        thres.add(threshold);
        tools.add(thres);
        return new JMenuItem[]{tools};
    }

    public void imageUpdated(ImageUpdateEvent event) {
      if(event.getId() == ImageUpdateEvent.NEW_IMAGE ){
        setVisible(false);
      }
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
