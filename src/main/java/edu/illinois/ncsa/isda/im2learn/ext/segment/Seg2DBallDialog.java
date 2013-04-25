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




import javax.swing.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageException;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.display.Im2LearnFrame;
import edu.illinois.ncsa.isda.im2learn.core.display.Im2LearnMenu;
import edu.illinois.ncsa.isda.im2learn.core.display.ImagePanel;
import edu.illinois.ncsa.isda.im2learn.core.display.ImageUpdateEvent;
import edu.illinois.ncsa.isda.im2learn.core.io.FileChooser;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageLoader;
import edu.illinois.ncsa.isda.im2learn.ext.info.MessageDialog;
import edu.illinois.ncsa.isda.im2learn.ext.misc.ImageFrame;
import edu.illinois.ncsa.isda.im2learn.ext.panel.GammaDialog;
import edu.illinois.ncsa.isda.im2learn.ext.panel.ZoomDialog;
import edu.illinois.ncsa.isda.im2learn.ext.vis.PseudoImageUI;

import java.awt.*;
import java.awt.event.*;
import java.net.MalformedURLException;
import java.net.URL;


public class Seg2DBallDialog extends Im2LearnFrame implements ActionListener, Im2LearnMenu {

	static private Log logger = LogFactory.getLog(Seg2DBall.class);
	
	JButton segBt, exitBt, applyBt, loadBt, saveSegBt, applyFinalBt, circBt, saveCircBt, autoSegBt, fgdBt, bkgBt;
	JTextField jf_seedX, jf_seedY, jf_threshold, jf_ballSize, jf_boundary, jf_circ, jf_seg, jf_minReg, jf_maxReg;
	JSlider js_band;

	ImageObject srcIm, segIm, finalIm, circIm;
	Seg2DBall seg;
	int currentIdx = 1;
	int seedX = -1, seedY = -1, ballSize = 1,band = 1, boundaryThickness = 10;
	double threshold = 30;

	 ImageFrame parent = null, myFrame = null, monitor = null, circMonitor = null;
   	
	 PseudoImageUI pseudo = new PseudoImageUI();
  
	 private ImagePanel imagepanel;
	 private JFrame finalFrm;


	 public Seg2DBallDialog() {

		 try {
			 jbInit();
		 }
		 catch(Exception e) {
			 e.printStackTrace();
		 }
    
	 }

  
	 public static void main(String args[]) {
		 try{
			 new Seg2DBallDialog();
		 }
		 catch(Exception e) {}
	  
	 }

	 @Override
	 public void closing() {
		 // frmPseudo.setVisible(false);  
	 }



	 @Override
	 public void showing() {
		 setImageObject(imagepanel.getImageObject());
	 }



	 public void setImageObject(ImageObject im) {
		 if(im != null) {
			 this.srcIm = im;
			 js_band.setMinimum(1);
			 js_band.setMaximum(srcIm.getNumBands());
			 js_band.setValue(1);
		 }
	 }

	 public void setSeed(int x, int y) {
		 seedX = x;
		 seedY = y;
		 jf_seedX.setText(String.valueOf(seedX));
		 jf_seedY.setText(String.valueOf(seedY));
	 }
	
	  public void setBoundaryThickness(int thickness) {
	    boundaryThickness = thickness;
	    this.jf_boundary.setText(String.valueOf(thickness));
	  }
	
	  public void setBallSize(int ball) {
	    this.ballSize = ball;
	    jf_ballSize.setText(String.valueOf(ballSize));
	  }

	  public void setThreshold(double thres) {
	    threshold = thres;
	    jf_threshold.setText(String.valueOf(threshold));
	  }
	
	  public void setBand (int b) {
	    this.band = b;
	    js_band.setValue(band);
	  }
	
	
	  private void jbInit() throws Exception {
	    segBt = new JButton();
	    autoSegBt = new JButton();
	    applyBt = new JButton();
	    exitBt = new JButton();
	    loadBt = new JButton();
	    saveSegBt = new JButton();
	    saveCircBt = new JButton();
	    applyFinalBt = new JButton();
	    circBt = new JButton();
	    fgdBt = new JButton();
	    bkgBt = new JButton();
	
	    segBt.setText("Segment");
	    autoSegBt.setText("Segment All");
	    applyBt.setText("Apply Final to Main");
	    exitBt.setText("Done");
	    loadBt.setText("Load");
	    saveSegBt.setText("Save");
	    saveCircBt.setText("Save");
	    applyFinalBt.setText("Apply to Final");
	    circBt.setText("Circumference");
	    fgdBt.setText("Foreground");
	    bkgBt.setText("Background");
	    
	    saveSegBt.setEnabled(false);
	    saveCircBt.setEnabled(false);	
	    this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
	    this.setTitle("Seg2DBallDialog");
	
	
	    jf_seedX = new JTextField("1");
	    jf_seedY = new JTextField("1");
	    jf_threshold = new JTextField(String.valueOf(threshold));
	    jf_ballSize = new JTextField("1");
	    jf_boundary = new JTextField("1");
	    jf_seg = new JTextField();
	    jf_seg.setEditable(false);
	    jf_circ = new JTextField();
	    jf_circ.setEditable(false);
	    jf_minReg = new JTextField("50");
	    jf_maxReg = new JTextField("5000");
	
	    
	    js_band = new JSlider(1,1);
	    js_band.setMajorTickSpacing(1);
	    js_band.setPaintLabels(true);
	    js_band.setPaintTicks(true);
	
	    loadBt.addActionListener(this);
	    saveSegBt.addActionListener(this);
	    saveCircBt.addActionListener(this);
	    applyFinalBt.addActionListener(this);
	    segBt.addActionListener(this);
	    autoSegBt.addActionListener(this);
	    applyBt.addActionListener(this);
	    exitBt.addActionListener(this);
	    circBt.addActionListener(this);
	    fgdBt.addActionListener(this);
	    bkgBt.addActionListener(this);
	
	    jf_seedX.addActionListener(this);
	    jf_seedY.addActionListener(this);
	    jf_threshold.addActionListener(this);
	    jf_ballSize.addActionListener(this);
	    jf_boundary.addActionListener(this);
	
	
	    JPanel optionP = new JPanel(new GridLayout(5,3));
	    JPanel alterMaskP = new JPanel();
	    optionP.setBorder(BorderFactory.createTitledBorder("Segment"));
	    alterMaskP.setBorder(BorderFactory.createTitledBorder("Manually alter result"));
	    
	    alterMaskP.add(fgdBt);
    	alterMaskP.add(bkgBt);
	    
	    optionP.add(new JLabel("Seed"));
	    optionP.add(jf_seedX);
	    optionP.add(jf_seedY);
	
	    optionP.add(new JLabel("Delta"));
	    optionP.add(jf_threshold);
	    optionP.add(new JLabel(""));
	//    optionP.add(autoThreshold);
	
	
	  //    optionP.add(new JLabel(""));
	
	    optionP.add(new JLabel("Ball Size"));
	    optionP.add(jf_ballSize);
	    optionP.add(new JLabel(""));
	    
	//    optionP.add(new JLabel("Boundary Thickness"));
	//    optionP.add(jf_boundary);
	    
	    optionP.add(segBt);
	    optionP.add(jf_seg);
	    optionP.add(saveSegBt);
	    optionP.add(circBt);
	    optionP.add(jf_circ);
	    optionP.add(saveCircBt);
	    
	
	    JPanel optionPAll = new JPanel(new GridLayout(2,3));
	    optionPAll.setBorder(BorderFactory.createTitledBorder("Segment All"));
	    
	    optionPAll.add(new JLabel("Min Region"));
	    optionPAll.add(new JLabel("Max Region"));
	    optionPAll.add(new JLabel(""));
	    optionPAll.add(jf_minReg);
	    optionPAll.add(jf_maxReg);
	    optionPAll.add(autoSegBt);
	    
	    JPanel bandP = new JPanel();
	    bandP.setBorder(BorderFactory.createTitledBorder("Band Selection"));
	    bandP.add(new JLabel("Band"));
	    bandP.add(js_band);
	
	    JPanel buttonP = new JPanel();
	
	//    if(parent == null)
	//      buttonP.add(loadBt);
	   // buttonP.add(saveBt);
	   // buttonP.add(saveFinalBt);
	  //  buttonP.add(segBt);
	   
	    buttonP.add(applyFinalBt);
	    buttonP.add(applyBt);
	    buttonP.add(exitBt);
	    
	    
	    JPanel segP = new JPanel(new BorderLayout());
	    segP.add(optionP, BorderLayout.NORTH);
	    segP.add(optionPAll, BorderLayout.SOUTH);
	    
	    this.getContentPane().add(segP, BorderLayout.NORTH);
	    this.getContentPane().add(bandP, BorderLayout.CENTER);
	    this.getContentPane().add(buttonP, BorderLayout.SOUTH);
	    this.getContentPane().add(alterMaskP,BorderLayout.EAST);
	
	    this.pack();
	  }
	
	
	  void load_actionPerformed() {
	    FileChooser file = new FileChooser();
	    
	    try {
	    	//String filename = file.showOpenDialog();
	    	//setImageObject(ImageLoader.readImage(filename));
	    	setImageObject(imagepanel.getImageObject());
	    	
	    	if(myFrame != null)
		      myFrame.dispose();
	    	Point winLoc = this.getLocation();
		    winLoc.x += this.getWidth();
		    myFrame = new ImageFrame("Source Image");
		    myFrame.setImageObject(srcIm);
		    myFrame.setVisible(true);
	    }
	    catch(Exception e) {
	    	System.out.println("File open failed");
	    }
	    
	
	  }
	
	  void saveSeg_actionPerformed() {
	    if(segIm == null) {
	      MessageDialog.displayMessage("Segmentation is not done");
	      return;
	    }
	    try{
	    	FileChooser file = new FileChooser();
	    	String filename = file.showSaveDialog();
	    	ImageLoader.writeImage(filename,segIm);
	    }
	    catch (Exception e) {
	      System.err.println("Output error");
	    }
	  }
	
	  void saveCirc_actionPerformed() {
		    if(circIm == null) {
		      MessageDialog.displayMessage("Segmentation is not done");
		      return;
		    }
		    try{
		    	FileChooser file = new FileChooser();
		    	String filename = file.showSaveDialog();
		    	ImageLoader.writeImage(filename,circIm);
		    }
		    catch (Exception e) {
		      System.err.println("Output error");
		    }
		  }
	  
	  void saveFinal_actionPerformed() {
	      if(finalIm == null) {
	        MessageDialog.displayMessage("Segmentation is not done");
	        return;
	      }
	      try{
	    	FileChooser file = new FileChooser();
	        String filename = file.showSaveDialog();
	        ImageLoader.writeImage(filename,segIm);
	      }
	      catch (Exception e) {
	        System.err.println("Output error");
	      }
	    }
	
	
	  void exit_actionPerformed() {
	    if(monitor != null)
	      monitor.dispose();
	    if(circMonitor != null)
	        circMonitor.dispose();
	    if(this.finalFrm != null)
	        finalFrm.dispose();
	    this.dispose();
	  }
	
	  
	  void applyToFinal_actionPerformed() {
		    if(segIm == null) {
		      MessageDialog.displayMessage("Segment image first");
		      return;
		    }
		    if(finalIm == null || segIm.getNumCols() != finalIm.getNumCols() || segIm.getNumRows() != finalIm.getNumRows()) {
		    	try{
		    		finalIm = ImageObject.createImage(segIm.getNumRows(), segIm.getNumCols(), 1, ImageObject.TYPE_INT);
		    	}
		    	catch(Exception e){}
		    }
	
		    for(int i=0; i<segIm.getSize(); i++) {
		    	if(segIm.getInt(i) != 0) {
		    		finalIm.set(i,currentIdx);
		   		}
		   	}
		    
		    currentIdx++;
	
	
	
		    pseudo.setSize(0,currentIdx);
		    pseudo.setImageObject(finalIm);
		    if(finalFrm == null) {
			    finalFrm = new Im2LearnFrame("Final Image");
			    finalFrm.add(pseudo);
			    finalFrm.pack();
			    Point pt = getLocation();
			    pt.x += this.getWidth();
			    finalFrm.setLocation(pt);
		    }
		    finalFrm.setVisible(true);
		   
	  }
	
	  
	  void apply_actionPerformed() {
	    if(finalIm == null) {
	      MessageDialog.displayMessage("Apply to Final first");
	      return;
	    }
	    imagepanel.setImageObject(finalIm);
	    imagepanel.setFakeRGBcolor(false);
	  }
	  void autoSeg_actionPerformed()  {
		  Seg2DBallAuto segAuto = new Seg2DBallAuto();
		  segAuto.setBallSize(Integer.parseInt(jf_ballSize.getText()));
		  segAuto.setDelta(Double.parseDouble(jf_threshold.getText()));
		  segAuto.setSegAreaLimit(Integer.parseInt(jf_minReg.getText()),Integer.parseInt(jf_maxReg.getText()));
		  try {
			segAuto.setImage(srcIm.extractBand(js_band.getValue()-1));
		  } catch (ImageException e) {
			  e.printStackTrace();
		  }
		  segAuto.runSegmentation();
		  
		  if(monitor == null) {
		    	monitor = new ImageFrame("Segmentation Result");
		    	Point pt = this.getLocation();
		    	logger.info(pt.x+","+pt.y);
		    	pt.y += this.getHeight();
		    	monitor.setLocation(pt);
		    	monitor.getImagePanel().addMenu(new ZoomDialog());
		    	monitor.getImagePanel().addMenu(new GammaDialog());
		    }
		  
		  	
		  
		    segIm = segAuto.getSegImage();
		    monitor.setImageObject(segIm);
		    monitor.getImagePanel().setFakeRGBcolor(false);
		    monitor.setVisible(true);
		    
		    try {
				finalIm = (ImageObject)segIm.clone();
				
				pseudo.setImageObject(finalIm);
			    if(finalFrm == null) {
				    finalFrm = new Im2LearnFrame("Final Image");
				    finalFrm.add(pseudo);
				    finalFrm.pack();
				    Point pt = getLocation();
				    pt.x += this.getWidth();
				    finalFrm.setLocation(pt);
			    }
			    finalFrm.setVisible(true);
		    } catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
		    saveSegBt.setEnabled(true);
	  }
	  
	  void seg_actionPerformed()  {
	
	    try {
	      setSeed(Integer.parseInt(jf_seedX.getText()),Integer.parseInt(jf_seedY.getText()));
	    }
	    catch(Exception e) {
	      System.err.println("Seed Value Error: Input integer values");
	    }
	
	    if(seedX<=0 || seedY<=0 || seedX>srcIm.getNumCols() || seedY>srcIm.getNumRows()) {
	      System.out.println("Error: Invalid seed");
	      return;
	    }
	
	    try{
	      setThreshold(Double.parseDouble(jf_threshold.getText()));
	    }
	    catch(Exception e) {
	      System.err.println("Threshold Value Erroe: Input integer values");
	    }
	
	    try{
	      setBallSize(Integer.parseInt(jf_ballSize.getText()));
	    }
	    catch(Exception ee) {
	      System.err.println("Ball Size Value Error: Input integer values");
	    }
	
	    try{
	      setBoundaryThickness(Integer.parseInt(jf_boundary.getText()));
	    }
	    catch(Exception ee) {
	      System.err.println("Boundary thickness Value Error: Input integer values");
	    }
	    band = js_band.getValue()-1;
	
	    seg = new Seg2DBall();
	    seg.setThreshold(threshold);
	    
	    try{
	    	seg.setImage(srcIm.extractBand(band));
	    	seg.setBallSeed(seedX, seedY, ballSize);
	//    	seg.setBallSize(ballSize);
	//    	seg.setSeed(seedX,seedY);
	    	seg.setThreshold(threshold);
	    }
	    catch(Exception e) {
	    	e.printStackTrace();
	    }
	
	    logger.info("Seed :"+seedX+","+seedY);
	    logger.info("BallSize :"+ballSize);
	    logger.info("Threshold :"+threshold);
	    logger.info("Band :"+band);
	    
	    
	    
	    seg.segment();
	    _done();	
	    jf_seg.setText(String.valueOf(seg.getPixelCount()));
	
	
	  }
	
	
	
	  void circ_actionPerformed() {
	
		  if(segIm == null) {
			  logger.warn("Segment an Image First");
			  return;
		  }
		  try {
			  seg.computeCircumference();
		  }
		  catch(Exception e){
			  logger.error("Computation failed");
			  return;
		  }
		  circIm = seg.getCircImageObject();
		  
	      this.jf_circ.setText(String.valueOf(seg.getCircPixelCount()));
	
	      if(circMonitor == null) {
	    	  Point winLoc = monitor.getLocation();
	    	  winLoc.x += monitor.getWidth();
	    	  circMonitor = new ImageFrame("Circumference");
	    	  circMonitor.setLocation(winLoc);
	    	  circMonitor.getImagePanel().addMenu(new ZoomDialog());
	      }
	      circMonitor.setImageObject(circIm);
	      circMonitor.setVisible(true);
	  }
	
	  void fgd_actionPerformed() {
		  Rectangle sel = monitor.getImagePanel().getSelection();
		  if(sel != null){
			  System.out.println("sel.width = " + sel.width);
			  for(int row = sel.y; row < sel.y + sel.height; row++){
				  for(int col = sel.x; col < sel.x + sel.width; col++){
					  segIm.set(row,col,0,1.0);
				  }
			  }
		  }
		  monitor.setImageObject(segIm);
		  monitor.getImagePanel().setFakeRGBcolor(false);
	  }
	  
	  void bkg_actionPerformed() {
		  Rectangle sel = monitor.getImagePanel().getSelection();
		  if(sel != null){
			  System.out.println("sel.width = " + sel.width);
			  for(int row = sel.y; row < sel.y + sel.height; row++){
				  for(int col = sel.x; col < sel.x + sel.width; col++){
					  segIm.set(row,col,0,0.0);
				  }
			  }
		  }
		  monitor.setImageObject(segIm);
		  monitor.getImagePanel().setFakeRGBcolor(false);
	  }
	  
	  void _done() {
	    band = js_band.getValue();
	    try {
	    	if(monitor == null) {
		    	monitor = new ImageFrame("Segmentation Result");
		    	Point pt = this.getLocation();
		    	logger.info(pt.x+","+pt.y);
		    	pt.y += this.getHeight();
		    	monitor.setLocation(pt);
		    	monitor.getImagePanel().addMenu(new ZoomDialog());
		    	monitor.getImagePanel().addMenu(new GammaDialog());
		    }
		    
	    	
	    	
		    segIm = seg.getSegImageObject();
		    monitor.setImageObject(seg.getSegImageObject());
		    monitor.getImagePanel().setFakeRGBcolor(false);
		    monitor.setVisible(true);
		    
		    saveSegBt.setEnabled(true);
		    saveCircBt.setEnabled(true);
	    }
	    catch(Exception e) {
	    	e.printStackTrace();
	    }
	  }
	
	
	  public void actionPerformed(ActionEvent e) {
	    Object obj = e.getSource();
	
	    if (obj == fgdBt) {
	    	fgd_actionPerformed();
	    }
	    if (obj == bkgBt) {
	    	bkg_actionPerformed();
	    }
	    if (obj == loadBt) {
	      load_actionPerformed();
	    }
	    if (obj == saveSegBt) {
	      saveSeg_actionPerformed();
	    }
	    if (obj == saveCircBt) {
	        saveCirc_actionPerformed();
	    }	
	    if (obj == applyFinalBt) {
	      applyToFinal_actionPerformed();
	    }
	    else if (obj == segBt) {
	      seg_actionPerformed();
	    }
	    else if (obj == autoSegBt) {
	        autoSeg_actionPerformed();
	    }
	    else if (obj == applyBt) {
	      apply_actionPerformed();
	    }
	    else if (obj == applyFinalBt) {
	        applyToFinal_actionPerformed();
	      }
	    else if (obj == exitBt) {
	      exit_actionPerformed();
	    }
	
	    
	    else if (obj == circBt) {
	      circ_actionPerformed();
	    }
	   
	  }
	
	  // ------------------------------------------------------------------------
	  // Im2LearnMenu implementation
	  // ------------------------------------------------------------------------
	  public void setImagePanel(ImagePanel imagepanel) {
	      this.imagepanel = imagepanel;
	   //   imagepanel.addAnnotationPanel(marker);
	     // setImageObject(imagepanel.getImageObject());
	  }
	
	  public JMenuItem[] getPanelMenuItems() {
	      return null;
	  }
	
	  public JMenuItem[] getMainMenuItems() {
		  JMenu menu = new JMenu("Tools");
		  JMenu segment = new JMenu("Segment");
		  segment.add(new JMenuItem(new AbstractAction("Segment 2D Ball") {
			public void actionPerformed(ActionEvent e) {
				Seg2DBallDialog.this.setVisible(true);
			}
		  }));
		  menu.add(segment);
		  return new JMenuItem[]{menu};
	  }
	
	  //private ImageMarker marker = new ImageMarker();
	  
	  public void imageUpdated(ImageUpdateEvent event) {
		 // logger.info("Event="+String.valueOf(event.getId()));
		  
		  if (event.getId() == ImageUpdateEvent.NEW_IMAGE) {
			  if (isVisible()) {
				  setImageObject(imagepanel.getImageObject());
			  }
		  } else if (event.getId() == ImageUpdateEvent.MOUSE_CLICKED) {
			  if(isVisible()) {
			  //MouseEvent e = (MouseEvent)event.getObject();
			//  logger.info("Event="+event.getObject());
				  Point pt = imagepanel.getImageLocationClicked();
				  setSeed((int)pt.getX(), (int)pt.getY());
	//		  marker.setLocation(pt);
	//		  marker.setVisible(true);
			  }
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
	
	
	
