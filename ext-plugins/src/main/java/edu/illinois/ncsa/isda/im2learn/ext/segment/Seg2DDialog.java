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
 * Created on Jul 29, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.illinois.ncsa.isda.im2learn.ext.segment;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

import javax.swing.*;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.datatype.LimitValues;
import edu.illinois.ncsa.isda.im2learn.core.display.Im2LearnFrame;
import edu.illinois.ncsa.isda.im2learn.core.display.Im2LearnMenu;
import edu.illinois.ncsa.isda.im2learn.core.display.ImagePanel;
import edu.illinois.ncsa.isda.im2learn.core.display.ImageUpdateEvent;
import edu.illinois.ncsa.isda.im2learn.core.io.FileChooser;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageLoader;
import edu.illinois.ncsa.isda.im2learn.ext.info.InfoDialog;
import edu.illinois.ncsa.isda.im2learn.ext.misc.ImEnhance;
import edu.illinois.ncsa.isda.im2learn.ext.misc.ImageFrame;
import edu.illinois.ncsa.isda.im2learn.ext.panel.SelectBandDialog;
import edu.illinois.ncsa.isda.im2learn.ext.panel.ZoomDialog;
import edu.illinois.ncsa.isda.im2learn.ext.segment.Seg2D;


/**
 * 	<p>
	<B>The class Seg2D provides a tool for automatic segmentation of two-dimensional multi-variate (multi-band) images. 
	 </B>
	 </p>

	 
<p> <b>Description:</b> This class performs segmentation of an image into color-homogeneous segments. The homogeneity or 
heterogeneity level is specified as sigma. The three edit boxes denoted as minsigma, maxsigma and deltasigma
denoted the homoegeneity level used when the image segmented first time (minsigma), last time (maxsigma) and with 
sigmas that are incremented by deltasigma (see the dialog below). 

  <p/>
  <img src="seg2DDialog.jpg"> <BR>
  <BR>

The segmentation is hierarchical in its nature and the exit criterion can be 
specified either by the minimum number of found segments (flagN2FindS is checked and the number of found segments is less than N2FindS in the 
corresponding edit box then exit) and/or by the maximum sigma. The former option is enforced by checking the flagN2FindS check box.
Using this option, an image can segmented as illustrated below.
<BR>
<BR>
  <img src="seg2DDialog1.jpg"> <BR>
  Input image.
  <BR>
<BR>
  <img src="seg2DDialog2.jpg"> <BR>
  Segmented image.
  <BR>
  
  <BR>


There is also an option to create a stack of segmentations by checking the check box labeled as flagN2FindLayers. With this option 
the range of maxsigma - minsigma will be divided into equal parts, where the number of parst is specified in the edit box
N2FindLayers. The resulting segmentation will contain N2FindLayers-deep segmentation hierarchy that represents the intermediate
segmentation results from minsigma to maxsigma.
<br>

 * @author Peter Bajcsy, Y-J. Lee
 *
 * TODO the ShowResult frame should enable band selection
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Seg2DDialog extends Im2LearnFrame implements Im2LearnMenu, ActionListener {


	  private JButton 			_cboxSegment 		= new JButton("Segment");
	  private JButton 			_cboxShowRes 		= new JButton("Show Result");
	  private JButton 			_cboxSaveRes 		= new JButton("Save Result");


	  private JButton 			_cboxDone 			= new JButton("Done");
	  private JLabel 			_labelResults 		= new JLabel("Results");
	  private String 			_fieldText 			= new String();

	  private JCheckBox 		_flagN2FindS 		= new JCheckBox("flagN2FindS");
	  private JLabel 			_labelN2FindS 		= new JLabel("N2FindS");
	  private JTextField 		_fieldN2FindS 		= new JTextField("100", 4);

	  private JCheckBox 		_flagN2FindLayers 	= new JCheckBox("flagN2FindLayers");
	  private JLabel 			_labelN2FindLayers	= new JLabel("N2FindLayers");
	  private JTextField 		_fieldN2FindLayers 	= new JTextField("5", 4);

	  //private FlyValidator _validatorN2FindS = new FlyValidator();

	  private JLabel 			_labelMinSigma 		= new JLabel("MinSigma");
	  private JTextField 		_fieldMinSigma 		= new JTextField("0.0", 4);
	  private JLabel 			_labelMaxSigma		= new JLabel("MaxSigma");
	  private JTextField 		_fieldMaxSigma 		= new JTextField("50.0", 4);
	  private JLabel 			_labelDeltaSigma 	= new JLabel("DeltaSigma");
	  private JTextField 		_fieldDeltaSigma 	= new JTextField("10.0", 4);


	  // TextARea(num of of rows, num of col)
	  private JTextArea 		_fieldResults 		= new JTextArea(15,30);

	  private String 			_title 				= "Seg2D";
	  private String 			_message 			= "Segmentation tool";
	  private boolean 			_isModal 			= false;//true;
	  private Seg2D 			_mySeg2D 			= new Seg2D();

	  private static Log 		logger 				= LogFactory.getLog(Seg2DDialog.class);

	  //private Dialog 			myd 				= null;
	  //private PlotDisplay _myPlot=null;
	  ///Frame mainFrame = null;
	  //ImageFrame 				_imFrame 			= null;

	  private ImageFrame 		_segLabelFrame 		= null;
	  //private ImageFrame _hierFrame = null;
	  //private JFrame _dbfFrame = new JFrame();

	  private ImageObject 		_imgObject 			= null;
	  //private GeoImageObject _geoImgObject = null;

	 // private ImInout _myImInout = new ImInout();
	  private LimitValues 		_lim 				= new LimitValues();

	  private Point 			_locShowImage 		= new Point();
	  String 					_frameTitle 		="Segment Image Attributes";

	  private ImEnhance 		_myImEnhance 		= new ImEnhance();
	  private ImagePanel 		imagepanel;
	  private ImageFrame 		imFrame				= null;

	  //////////////////////////////////////////////////
	  // the main constructor
	  public Seg2DDialog(){
	  	super("Seg 2D");
	    /*if(imagepanel != null)
	  	_imgObject = imagepanel.getImageObject();

	  	if(_imgObject == null){
	      System.out.println("Error: no image to process !");
	      return;
	    }*/

	  	/*imFrame = new ImageFrame("Image");
	    if(imFrame == null){
	      System.out.println("Error: no image frame !");
	      return;
	    }*/

	   // _imgObject = imgObject;//imFrame.GetImagePanel().GetSourceObject();
	    //_imFrame = imFrame;
	    // set all the values
	    _mySeg2D.SetMinSigma(10.0F);
	    _mySeg2D.SetMaxSigma(100.0F);
	    _mySeg2D.SetDeltaSigma(10.0F);

	    _mySeg2D.SetN2FindS(100);
	    _mySeg2D.SetFlagEvenSigmaLayers(true);
	    _mySeg2D.SetN2FindEvenSigmaLayers(5);


	    //_fieldN2Find.setNextFocusableComponent(_fieldMinSigma);
	    _fieldMinSigma.setNextFocusableComponent(_fieldMaxSigma);
	    _fieldMaxSigma.setNextFocusableComponent(_fieldDeltaSigma);
	    _fieldDeltaSigma.setNextFocusableComponent(_fieldMinSigma);

	    _flagN2FindS.setSelected( _mySeg2D.GetFlagN2FindS() );
	    _flagN2FindS.setEnabled( true);
	    _flagN2FindS.setFont(_flagN2FindS.getFont().deriveFont(_flagN2FindS.getFont().PLAIN) );
	    _fieldN2FindS.setText(Integer.toString(_mySeg2D.GetN2FindS()));

	    _flagN2FindLayers.setSelected(_mySeg2D.GetFlagEvenSigmaLayers() );
	    _flagN2FindLayers.setEnabled(true );
	    _flagN2FindLayers.setFont(_flagN2FindLayers.getFont().deriveFont(_flagN2FindLayers.getFont().PLAIN) );
	    _fieldN2FindLayers.setText(Integer.toString(_mySeg2D.GetN2FindEvenSigmaLayers() ) );

	    _fieldMinSigma.setText(Float.toString(_mySeg2D.GetMinSigma()));
	    _fieldMaxSigma.setText(Float.toString(_mySeg2D.GetMaxSigma()));
	    _fieldDeltaSigma.setText(Float.toString(_mySeg2D.GetDeltaSigma()));


	    Seg2DDialogPrep();
	  }

	 /* private void Seg2DDialog(){

	    //prepare dialog
	    //_myPlot = new PlotDisplay();
	    //_myPlot.frame = null;



	 }*/
	  public void Seg2DDialogPrep(){

	  	//imFrame = new ImageFrame("Dialog");
	    //myd = new Dialog(imFrame, title,isModal);

	    _locShowImage.x = 0;
	    _locShowImage.y = 0;

	    //_myShowImage = new LabelFrame(_imgObject, _locShowImage, _frameTitle);
	    //_myShowImage = new LabelFrame(_locShowImage, _frameTitle);
	    //_myShowImage.setVisible(false);

	    Box vbox = Box.createVerticalBox();
	    JPanel donePanel = new JPanel();
	    JPanel resultsPanel = new JPanel();
	    JPanel buttonPanel = new JPanel();
	    JPanel stopPanel = new JPanel();

	    stopPanel.setLayout(new GridLayout(6,1) );
	    stopPanel.add(_flagN2FindS);
	    stopPanel.add(_labelN2FindS);
	    stopPanel.add(_fieldN2FindS);

	    stopPanel.add(_flagN2FindLayers);
	    stopPanel.add(_labelN2FindLayers);
	    stopPanel.add(_fieldN2FindLayers);

	    //selectPanel.add(stopPanel);

	    //resultsPanel.setLayout(new GridLayout(2,1) );
	    //resultsPanel.add(sortPanel);

	    //resultsPanel.add(_labelResults);
	    _fieldResults.setEditable(false);
	    _fieldResults.setBackground(Color.lightGray);
	    resultsPanel.add(_fieldResults);

	    buttonPanel.setLayout(new GridLayout(4,3) );

	    //buttonPanel.add(_labelN2Find);
	    buttonPanel.add(_labelMinSigma);
	    buttonPanel.add(_labelMaxSigma);
	    buttonPanel.add(_labelDeltaSigma);

	    //_fieldN2Find.addKeyListener(_validatorN2Find);
	     //    buttonPanel.add(_fieldN2Find);
	    //_fieldMinSigma.addKeyListener(_validatorAccept);
	    buttonPanel.add(_fieldMinSigma);
	    buttonPanel.add(_fieldMaxSigma);
	    buttonPanel.add(_fieldDeltaSigma);

	    buttonPanel.add(_cboxSegment);
	    buttonPanel.add(_cboxShowRes);
	    buttonPanel.add(_cboxSaveRes);
	    buttonPanel.add(_labelResults);

	    //done Panel
	    donePanel.add(_cboxDone);

	   // myd.setLayout(new GridLayout(3,1,10,50));
	    //myd.setLayout(new GridLayout(2,2));

	    //myd.setLayout(new FlowLayout(FlowLayout.CENTER,0,0) );
	    vbox.add(stopPanel);

	    buttonPanel.setSize(240,100);
	    vbox.add(buttonPanel);
	    vbox.add(resultsPanel);
	    vbox.add(donePanel);

	    //myd.setResizable(true);

	    //myd.setBounds(200,200,450,520);
	    //myd.setBounds(200,200,240,500);

	    //myd.setLayout(new BorderLayout() );
	    //myd.add(buttonPanel, "North");
	    //myd.add(resultsPanel, "Center");
	    //myd.add(donePanel, "South");

	    //action listeners
	     _flagN2FindS.addItemListener(new ItemListener() {
	        public void itemStateChanged(ItemEvent event){
	            _mySeg2D.SetFlagN2FindS(_flagN2FindS.isSelected() );
	        }
	     });
	     _flagN2FindLayers.addItemListener(new ItemListener() {
	        public void itemStateChanged(ItemEvent event){
	            _mySeg2D.SetFlagEvenSigmaLayers(_flagN2FindLayers.isSelected() );
	        }
	     });

	     //////////////////
	    _fieldN2FindS.addActionListener(new ActionListener() {
	       public void actionPerformed(ActionEvent event) {
	         String s1 = _fieldN2FindS.getText();
	         logger.debug("test inside fieldN2Find: getText ="+s1);
	         int temp;
	         try{
	           temp = Integer.parseInt(s1);
	         }
	         catch(NumberFormatException e){
	         	logger.debug("Error: Enter a number ! ");
	           _fieldN2FindS.setText(Integer.toString(_mySeg2D.GetN2FindS()));
	           return;
	         }
	         if( !_mySeg2D.SetN2FindS(temp) ){
	          // set the former value
	          _fieldN2FindS.setText(Integer.toString(_mySeg2D.GetN2FindS()));
	         }

	      }
	     });
	    _fieldN2FindLayers.addActionListener(new ActionListener() {
	       public void actionPerformed(ActionEvent event) {
	         String s1 = _fieldN2FindLayers.getText();
	         logger.debug("test inside fieldN2FindLayers: getText ="+s1);
	         int temp;
	         try{
	           temp = Integer.parseInt(s1);
	         }
	         catch(NumberFormatException e){
	         	logger.debug("Error: Enter a number ! ");
	           _fieldN2FindLayers.setText(Integer.toString(_mySeg2D.GetN2FindEvenSigmaLayers()));
	           return;
	         }
	         if( !_mySeg2D.SetN2FindEvenSigmaLayers(temp) ){
	          // set the former value
	          _fieldN2FindLayers.setText(Integer.toString(_mySeg2D.GetN2FindEvenSigmaLayers()));
	         }

	      }
	     });

	     _fieldMinSigma.addActionListener(new ActionListener() {
	       public void actionPerformed(ActionEvent event) {
	         String s1 = _fieldMinSigma.getText();
	         logger.debug("test inside fieldMinSigma: getText ="+s1);
	         float temp;
	         try{
	           temp = Float.parseFloat(s1);
	         }
	         catch(NumberFormatException e){
	         	logger.debug("Error: Enter a number ! ");
	           _fieldMinSigma.setText(Float.toString(_mySeg2D.GetMinSigma()));
	           return;
	         }
	         if( !_mySeg2D.SetMinSigma(temp) ){
	          // set the former value
	          _fieldMinSigma.setText(Float.toString(_mySeg2D.GetMinSigma()));
	         }

	      }
	     });
	     _fieldMaxSigma.addActionListener(new ActionListener() {
	       public void actionPerformed(ActionEvent event) {
	         String s1 = _fieldMaxSigma.getText();
	         logger.debug("test inside fieldMaxSigma: getText ="+s1);
	         float temp;
	         try{
	           temp = Float.parseFloat(s1);
	         }
	         catch(NumberFormatException e){
	         	logger.debug("Error: Enter a number ! ");
	           _fieldMaxSigma.setText(Float.toString(_mySeg2D.GetMaxSigma()));
	           return;
	         }
	         if( !_mySeg2D.SetMaxSigma(temp) ){
	          // set the former value
	          _fieldMaxSigma.setText(Float.toString(_mySeg2D.GetMaxSigma()));
	         }

	      }
	     });
	     _fieldDeltaSigma.addActionListener(new ActionListener() {
	       public void actionPerformed(ActionEvent event) {
	         String s1 = _fieldDeltaSigma.getText();
	         logger.debug("test inside fieldDeltaSigma: getText ="+s1);
	         float temp;
	         try{
	           temp = Float.parseFloat(s1);
	         }
	         catch(NumberFormatException e){
	           logger.debug("Error: Enter a number ! ");
	           _fieldDeltaSigma.setText(Float.toString(_mySeg2D.GetDeltaSigma()));
	           return;
	         }
	         if( !_mySeg2D.SetDeltaSigma(temp) ){
	          // set the former value
	          _fieldDeltaSigma.setText(Float.toString(_mySeg2D.GetDeltaSigma()));
	         }
	      }
	     });


	    _cboxSegment.addActionListener(this);
	    _cboxShowRes.addActionListener(this);
	    _cboxSaveRes.addActionListener(this);

	    _cboxDone.addActionListener(this);

        getContentPane().add(vbox);
	    //myd.addWindowListener(new Seg2DDialogWindowListener() );

	    pack();

	    //myd.setVisible(true);
	    //myd.setBounds(int x, int y, width, height)
	    //myd.setBounds(200,200,200,460);
	    //myd.setBounds(200,200,280,600);

	    //myd.show();


	  }


	  public void actionPerformed(ActionEvent event) {

	    JButton cbox = (JButton)event.getSource();

	    if(cbox == _cboxSegment)
	      SegmentActivated(event);
	    if(cbox == _cboxShowRes)
	      ShowResActivated(event);
	    if(cbox == _cboxSaveRes)
	      SaveResActivated(event);

	    if (cbox == _cboxDone)
	      DoneActivated(event);

	    //System.out.println("inof:getEchoChar="+_fieldRow.getEchoChar());
	  }
	  ///////////////////////////////////////////////////////////////////////////////
	  // execute button actions
	  ///////////////////////////////////////////////////////////////////////////////
	  public void SegmentActivated (ActionEvent event) {
	    setCursor(new Cursor(Cursor.WAIT_CURSOR));
	    //System.out.println("Test: inside Segment");

	    //String s = new String();
	    _fieldText = "Segmenting \n";
	    _fieldResults.setText(_fieldText);

	    //test
	    //System.out.println("Test: SegmentActivated _dbfTable="+_dbfTable);

	    if( !_mySeg2D.nonTexture_Segm( imagepanel.getImageObject() ) ){
	       _fieldText += "ERROR: could not perform \n NonTexture Segmentation without errors \n";
	      _fieldResults.setText(_fieldText);
	       //System.err.println("ERROR: could not perform Search");
	       imagepanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	       return;
	    }
	    _fieldText = "Finished Segmenting \n";
	    _fieldResults.setText(_fieldText);

	    if(_mySeg2D.GetImLabels() == null)
	    	logger.debug("ERROR: NO LABELS");
	    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	  }

	   ////////////////////////////////////////////////////////////////////////////
	   // show results in the text field and as a plot of row/col coordinates = f(image index)
	   //////////////////////////////////////////////////////////
	    public void ShowResActivated (ActionEvent event) {

	    setCursor(new Cursor(Cursor.WAIT_CURSOR));

	    //System.out.println("Test: ShowRes");
	    //String s = new String();
	    // clear the field
	    _fieldText = "Segmentation Results \n";
	    _fieldText += "NFound Segments ="+ _mySeg2D.GetNFoundS()+"\n";
	    int i;
	    /*
	    ImageObject mergeStats = _mySeg2D.GetMergeStats();
	    if(mergeStats != null ){
	      if( mergeStats.numcols < 100 ){
	        for(i = 0; i< mergeStats.size;i+= mergeStats.sampPerPixel){
	            if(mergeStats.imageDouble[i] > 0)
	              _fieldText +="NFound="+(float)mergeStats.imageDouble[i+2]+", Sigma ="+(float)mergeStats.imageDouble[i]+", MinMerger="+ (float)mergeStats.imageDouble[i+1] +"\n";
	        }
	      }else{
	        int offset = (int) (mergeStats.numcols/100);
	        offset *= mergeStats.sampPerPixel;
	        for(i = 0; i< mergeStats.size;i+= offset){
	            if(mergeStats.imageDouble[i] > 0)
	              _fieldText +="NFound="+(float)mergeStats.imageDouble[i+2]+", Sigma ="+(float)mergeStats.imageDouble[i]+", MinMerger="+ (float)mergeStats.imageDouble[i+1] +"\n";
	        }
	      }
	    }
	    */
	    _fieldResults.setText(_fieldText);


	    // test if there is hierarchical representation of labels
	    if(_mySeg2D.GetImLabels() == null  ){
	      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	      logger.debug("ERROR: No ImLabels");
	      return;
	    }
	    // show the hierarchical representation
	    int maxLabel = _mySeg2D.GetNFoundS() + 1;
	    if(_mySeg2D.GetFlagEvenSigmaLayers() ){
	      maxLabel = _mySeg2D.GetImLabels().getNumRows() * _mySeg2D.GetImLabels().getNumCols() + 1;// (_mySeg2D.GetMaxN2FoundSLayers()+1);
	    }

	    _myImEnhance.EnhanceLabelsIn(_mySeg2D.GetImLabels(),maxLabel,true );
	    if( _myImEnhance.GetEnhancedObject() == null){
	       //_fieldText += "ERROR: could not perform \nSegmentGeoPts without errors \n";
	       //_fieldResults.setText(_fieldText);
	    	logger.debug("ERROR: could not enhance labels");
	        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	       return;
	    }

	    Point locLabelImage = new Point(_locShowImage);
	    locLabelImage.move(200,100);
	    if(_segLabelFrame == null){
	      if( _myImEnhance.GetEnhancedObject() != null){
	      	logger.debug("TEST: enhanced label image");
	        _segLabelFrame = new ImageFrame("SegmentLabel Image");
	        _segLabelFrame.getImagePanel().addMenu(new SelectBandDialog());
	        _segLabelFrame.getImagePanel().addMenu(new InfoDialog());
	        _segLabelFrame.getImagePanel().addMenu(new ZoomDialog());
	        
	        _segLabelFrame.setImageObject(_myImEnhance.GetEnhancedObject());
	        _segLabelFrame.setVisible(true);
	      }else{
	      	logger.debug("TEST: could not enahnce the Segm. Label image");
	        //_segLabelFrame = new ImageFrame(_mySeg2D.GetImLabels() ,locLabelImage,"SegmentLabel Image");
	         _segLabelFrame = new ImageFrame("SegmentLabel Image");
	        _segLabelFrame.getImagePanel().addMenu(new SelectBandDialog());
	        _segLabelFrame.getImagePanel().addMenu(new InfoDialog());
	        _segLabelFrame.getImagePanel().addMenu(new ZoomDialog());

	        _segLabelFrame.setImageObject(_mySeg2D.GetImLabels());
	         _segLabelFrame.setVisible(true);
	      }
	    }else{
	      if( _myImEnhance.GetEnhancedObject() != null){
	      	logger.debug("TEST: enhanced SegmentLabel image");
	       _segLabelFrame.setImageObject(_myImEnhance.GetEnhancedObject() );
	      }else{
	      	logger.debug("TEST: could not enhance  segmentation label image");
	       //_segLabelFrame.ImageOpen(_mySeg2D.GetImLabels() );
	        _segLabelFrame.setImageObject(_mySeg2D.GetImLabels() );
	      }
	    }

	    if(_segLabelFrame != null)
	    	_segLabelFrame.setVisible(true);

	    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	  }

	   //////////////////////////////////////////////////////////////////

	   public void SaveResActivated (ActionEvent event) {

	     setCursor(new Cursor(Cursor.WAIT_CURSOR));
	    //test
	     logger.debug("Test: inside SaveResActivated");

	    if(_mySeg2D.GetImLabels() == null  ){
	       _fieldText = "No Segmentation Label Results\n";
	       _fieldResults.setText(_fieldText);
	       // System.out.println("Error: no results");
	        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	        return;
	    }
	    // ask for the output file name
	    String FileName=null;
	    //FileDialogIm2Learn dialog = new FileDialogIm2Learn(_imFrame, "Save Segment Label Results",FileDialogIm2Learn.SAVE);
	    FileChooser fc = new FileChooser();
	    fc.setTitle("Save Segment Label Results");

	    fc.setSelectedFile("default.iip");
	    try {
	        FileName = fc.showSaveDialog();
	    } catch (Exception exc) {
	        exc.printStackTrace();
	        FileName = null;
	    }

	     if(FileName != null) {
	        try{
	           _fieldText = "Saving Results \n";
	           _fieldResults.setText(_fieldText);
	              try {
	              		ImageLoader.writeImage(FileName, _segLabelFrame.getImagePanel().getImageObject());
	              } catch (Exception e) {
	              		_fieldText = "Error: Save Segment Label Image failed\n";
	              		logger.debug("Error: Save Segment Label Image failed");
	              		//contentPane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	              		//return;
	              }

	           _fieldText = "Finished Saving Results \n";
	           _fieldResults.setText(_fieldText);

	         }
	          catch(Exception e){
	          	logger.debug("Error: IO Exception");
	          }
	       }else{
	       	logger.debug("Info: FileDialogIm2Learn Cancelled");
	          //return;
	       }

	      // set the hour glass for slow saving
	      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	  }



	  public void DoneActivated (ActionEvent event) {
	    if(_segLabelFrame != null && _segLabelFrame.isVisible() ){
	       _segLabelFrame.dispose();
	    }
	    hide();
	  }



	  class Seg2DDialogWindowListener extends WindowAdapter {
	    public void windowClosing(WindowEvent event) {
	      Window window = (Window)event.getSource();
	      window.dispose();

	      if(_segLabelFrame != null && _segLabelFrame.isVisible() ){
	         _segLabelFrame.dispose();
	      }


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
	        segment.add(new JMenuItem(new AbstractAction("Segment 2D") {
	            public void actionPerformed(ActionEvent e) {
	                if (!isVisible()) {
	                    Window win = SwingUtilities.getWindowAncestor(imagepanel);
	                    setLocationRelativeTo(win);
	                    setVisible(true);
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
            return getClass().getResource("help/Seg2DDialog.html");
	    }

}
