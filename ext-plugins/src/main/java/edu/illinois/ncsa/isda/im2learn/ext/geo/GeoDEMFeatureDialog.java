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
package edu.illinois.ncsa.isda.im2learn.ext.geo;
/*
 * GISOperDialog.java
 *
 */



/**
 *
 * @author Peter Bajcsy
 * @version 1.0
 */


import javax.swing.*;

import edu.illinois.ncsa.isda.im2learn.core.display.Im2LearnFrame;
import edu.illinois.ncsa.isda.im2learn.core.display.Im2LearnMenu;
import edu.illinois.ncsa.isda.im2learn.core.display.ImagePanel;
import edu.illinois.ncsa.isda.im2learn.core.display.ImageUpdateEvent;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageLoader;
import edu.illinois.ncsa.isda.im2learn.ext.misc.ImageFrame;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URL;

public class GeoDEMFeatureDialog extends Im2LearnFrame implements ActionListener, Im2LearnMenu  {

  private ImagePanel imagepanel = null;
  private JButton _cboxDone = new JButton("Done");
  private JButton _cboxShow = new JButton("Show");
  private JButton _cboxSave = new JButton("Save");

  private Label _labelEdgeType = new Label("GIS Operation");
  private Choice _choiceEdgeType = new Choice();
  private String _choiceText1 = "Slope";
  private String _choiceText2 = "Aspect";
  private String _choiceText3 = "Curvature";
  private String _choiceText4 = "FlowDirection";
  private String _choiceText5 = "FlowAccum";
  private String _choiceText6 = "Watershed";
  private String _choiceText7 = "Sink";
  private String _choiceText8 = "FillDepression";
  private String _choiceText9 = "CTI";

  private int _edgeType = 0;
  private String _edgeTypeStr= new String();
  private int _kernelRow = 10;
  private int _kernelCol = 10;

  private String _title = "Features";
  private String _message = "Feature Computation";
  private boolean _isModal = false;
  //private Edge myEdge = new Edge();
  private GeoDEMFeature myGISOper = new GeoDEMFeature();
  //private Morpho myMorpho = new Morpho();
  //private SpFilters mySpFilters = new SpFilters();

  //private Dialog myd = null;
  private ImageFrame myShowImage=null;
  private Point _locShowImage = new Point();

  //private GeoImageObject _imgObject;
  //private ImageFrame mainFrame = null;

  //private TwoEditDialog _twoEditDialog = null;

  public GeoDEMFeatureDialog() {

/*    if(imgObject.image == null && imgObject.imageShort == null
      && imgObject.imageInt == null && imgObject.imageFloat == null){
      System.out.println("Error: no BYTE or SHORT or INT or FLOAT image to process !");
      return;
    }
    */
   super("DEM Features");

   PopulateChoice();
   createUI();


  }

   // populate the choice menu
  private void PopulateChoice(){
    int i;
    _choiceEdgeType.add(_choiceText1);
    _choiceEdgeType.add(_choiceText2);
    _choiceEdgeType.add(_choiceText3);
    _choiceEdgeType.add(_choiceText4);
    _choiceEdgeType.add(_choiceText5);
    _choiceEdgeType.add(_choiceText6);
    _choiceEdgeType.add(_choiceText7);
    _choiceEdgeType.add(_choiceText8);
    _choiceEdgeType.add(_choiceText9);
  }

  private void createUI() {

    // /GISOperDialog(myFrame,_title, _isModal);


    //set default values
    _edgeType = 0; // SobelMagOrient
    _edgeTypeStr = _choiceText1;

    myShowImage = new ImageFrame("EdgeFeatures");
    myShowImage.setVisible(false);

    //myd = new Dialog(myFrame,title,isModal);


    Panel typePanel = new Panel();
    typePanel.setLayout(new GridLayout(2,1) );
    typePanel.add(_labelEdgeType);
    typePanel.add(_choiceEdgeType);


    Panel donePanel = new Panel();
    //donePanel.setLayout(new GridLayout(2,1) );
    //donePanel.setLayout(new BorderLayout() );
    //donePanel.setLayout(new FlowLayout(FlowLayout.CENTER,10,5) );
    //_fieldThreshVector.setSize(350,50);
    //donePanel.add(_fieldThreshVector);
    donePanel.add(_cboxShow);
    donePanel.add(_cboxSave);
    donePanel.add(_cboxDone);

    //_cboxShow.setLocation(10,10);

    //sliderPanel.add(_controlPanel);
    /*
    myd.setLayout(new BorderLayout() );
    myd.add(typePanel, "North");
    myd.add(sliderPanel, "Center");
    //myd.add(fieldPanel,"South");
    myd.add(donePanel, "South");
    //myd.add(donePanel, "East");
    //myd.add(_cboxDone);
    //myd.add(_cboxShow);
    */

    //myd.setLayout(new GridLayout(4,1) );
    getContentPane().setLayout(new FlowLayout(FlowLayout.CENTER,0,0) );
    getContentPane().setBounds(200,200,250,120);
    //typePanel.setSize(340,100);
    getContentPane().add(typePanel);
    getContentPane().add(donePanel);
    //setResizable(true);


    _choiceEdgeType.select(_edgeType);


    //_choiceThreshType.addItemListener(listenerThreshType);
    _choiceEdgeType.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent event) {
        int index = _choiceEdgeType.getSelectedIndex();
        _edgeTypeStr = _choiceEdgeType.getSelectedItem();
        if(_edgeTypeStr.equalsIgnoreCase(_choiceText1)) {
          if(myShowImage.isVisible()){
             myGISOper.ComputeSlope(imagepanel.getImageObject(),3);
             myShowImage.setImageObject(myGISOper.GetSlope());
          }



        }
        if(_edgeTypeStr.equalsIgnoreCase(_choiceText2)) {
           if(myShowImage.isVisible()){
             myGISOper.ComputeSlope(imagepanel.getImageObject(),3);
             myShowImage.setImageObject(myGISOper.GetAspect());
           }

        }

        if(_edgeTypeStr.equalsIgnoreCase(_choiceText3)) {
           if(myShowImage.isVisible()){
                myGISOper.ComputeCurvature(imagepanel.getImageObject(),3);
                myShowImage.setImageObject(myGISOper.GetCurvature());
           }
        }
        if(_edgeTypeStr.equalsIgnoreCase(_choiceText4)) {
           if(myShowImage.isVisible()){
                myGISOper.ComputeFlowDirection(imagepanel.getImageObject());
                myShowImage.setImageObject(myGISOper.GetFlowDirection());
           }
        }
        if(_edgeTypeStr.equalsIgnoreCase(_choiceText5)) {
           if(myShowImage.isVisible()){
                myGISOper.ComputeFlowAccumulation(imagepanel.getImageObject());
                myShowImage.setImageObject(myGISOper.GetFlowAccum());
           }
        }
        if(_edgeTypeStr.equalsIgnoreCase(_choiceText6)) {
          if(myShowImage.isVisible()){
              myGISOper.FindWatershed(imagepanel.getImageObject());
              myShowImage.setImageObject(myGISOper.GetWatershed());
         }

        }
        if(_edgeTypeStr.equalsIgnoreCase(_choiceText7)) {
          if(myShowImage.isVisible()){
              myGISOper.ComputeSinks(imagepanel.getImageObject());
              myShowImage.setImageObject(myGISOper.GetSink());
         }

        }

        if(_edgeTypeStr.equalsIgnoreCase(_choiceText8)) {
          if(myShowImage.isVisible()){
               myGISOper.FillDepression(imagepanel.getImageObject());
               myShowImage.setImageObject(myGISOper.GetDepressionFilled());
          }

        }
        if(_edgeTypeStr.equalsIgnoreCase(_choiceText9)) {
          if(myShowImage.isVisible()){
               myGISOper.ComputeCTI(imagepanel.getImageObject());
               myShowImage.setImageObject(myGISOper.GetCTI());
          }

        }



      }
    });


    _cboxDone.addActionListener(this);
    _cboxShow.addActionListener(this);
    _cboxSave.addActionListener(this);

    //myd.addWindowListener(new EdgeDialogWindowListener() );

    pack();
    //myd.show();

  }

  public void closing(){
      dispose();
      if(myShowImage.isVisible()){
        myShowImage.closing();
        myShowImage.dispose();
      }

  }


  public void actionPerformed(ActionEvent event) {
    //Button cbox = (Button)event.getSource();
    if ((JButton)event.getSource() == _cboxDone)
      DoneActivated(event);
    //System.out.println("inof:getEchoChar="+_fieldRow.getEchoChar());
    if ((JButton)event.getSource() == _cboxShow)
      ShowActivated(event);
    if ((JButton)event.getSource() == _cboxSave)
      SaveActivated(event);

  }

  public void DoneActivated (ActionEvent event) {
    //_areaDialog = new SubArea();
    //System.out.println("test inside endian =" + endian);
    dispose();
    myShowImage.dispose();
  }
  public void ShowActivated (ActionEvent event) {

    //System.out.println("test inside showActivated; Visible="+myShowImage.isVisible());
    if(myShowImage.isVisible()){
       myShowImage.setVisible(false);
    }else{
      //test
        //System.out.println("test inside showActivated; _edgeTypeStr="+_edgeTypeStr );
     if(_edgeTypeStr.equalsIgnoreCase(_choiceText1)) {
        myShowImage.setVisible(true);
        if(myShowImage.isVisible()){
              System.out.println("Performing Slope Operation");
              System.out.println("Image Col Res : " + imagepanel.getImageObject().getNumCols() + "   Row Res : "
                                 + imagepanel.getImageObject().getNumRows());
              myGISOper.ComputeSlope(imagepanel.getImageObject(),3);
              myShowImage.setImageObject(myGISOper.GetSlope());
           }

      }
      if(_edgeTypeStr.equalsIgnoreCase(_choiceText2)) {
        myShowImage.setVisible(true);
        if(myShowImage.isVisible()){
              System.out.println("Performing Aspect Operation");
              System.out.println("Image Col Res : " + imagepanel.getImageObject().getNumCols() + "   Row Res : "
                                 + imagepanel.getImageObject().getNumRows());
              myGISOper.ComputeSlope(imagepanel.getImageObject(),3);
              myShowImage.setImageObject(myGISOper.GetAspect());
           }

      }

      if(_edgeTypeStr.equalsIgnoreCase(_choiceText3)) {
        myShowImage.setVisible(true);
        if(myShowImage.isVisible()){
             System.out.println("Performing Curvature Operation");
             System.out.println("Image Col Res : " + imagepanel.getImageObject().getNumCols() + "   Row Res : "
                                + imagepanel.getImageObject().getNumRows());
             myGISOper.ComputeCurvature(imagepanel.getImageObject(),3);
             myShowImage.setImageObject(myGISOper.GetCurvature());

        }
      }

      if(_edgeTypeStr.equalsIgnoreCase(_choiceText4)) {
        myShowImage.setVisible(true);
        if(myShowImage.isVisible()){
          System.out.println("Performing Curvature Operation");
            System.out.println("Image Col Res : " + imagepanel.getImageObject().getNumCols() + "   Row Res : "
                               + imagepanel.getImageObject().getNumRows());
            myGISOper.ComputeFlowDirection(imagepanel.getImageObject());
            myShowImage.setImageObject(myGISOper.GetFlowDirection());


        }

      }

       if(_edgeTypeStr.equalsIgnoreCase(_choiceText5)) {
        myShowImage.setVisible(true);
        if(myShowImage.isVisible()){
          System.out.println("Performing Curvature Operation");
           System.out.println("Image Col Res : " + imagepanel.getImageObject().getNumCols() + "   Row Res : "
                              + imagepanel.getImageObject().getNumRows());
           myGISOper.ComputeFlowAccumulation(imagepanel.getImageObject());
           myShowImage.setImageObject(myGISOper.GetFlowAccum());

          }
        }
       if(_edgeTypeStr.equalsIgnoreCase(_choiceText6)) {
        myShowImage.setVisible(true);
        if(myShowImage.isVisible()){
         System.out.println("Performing Watershed Operation");
          System.out.println("Image Col Res : " + imagepanel.getImageObject().getNumCols() + "   Row Res : "
                             + imagepanel.getImageObject().getNumRows());
          System.out.println("Test Watershed?");
          myGISOper.FindWatershed(imagepanel.getImageObject());
          myShowImage.setImageObject(myGISOper.GetWatershed());

         }

        }
       if(_edgeTypeStr.equalsIgnoreCase(_choiceText7)) {
         myShowImage.setVisible(true);
        if(myShowImage.isVisible()){
         System.out.println("Performing Sink Finding");
          System.out.println("Image Col Res : " + imagepanel.getImageObject().getNumCols() + "   Row Res : "
                             + imagepanel.getImageObject().getNumRows());
          myGISOper.ComputeSinks(imagepanel.getImageObject());
          myShowImage.setImageObject(myGISOper.GetSink());

         }

        }


        if(_edgeTypeStr.equalsIgnoreCase(_choiceText8)) {
          myShowImage.setVisible(true);
          if(myShowImage.isVisible()){
          System.out.println("Performing Depression Filling");
          System.out.println("Image Col Res : " + imagepanel.getImageObject().getNumCols() + "   Row Res : "
                             + imagepanel.getImageObject().getNumRows());
          myGISOper.FillDepression(imagepanel.getImageObject());
          myShowImage.setImageObject(myGISOper.GetDepressionFilled());

          }

        }


        if(_edgeTypeStr.equalsIgnoreCase(_choiceText9)) {
          myShowImage.setVisible(true);
          if(myShowImage.isVisible()){
          System.out.println("Performing CTI Computing");
          System.out.println("Image Col Res : " + imagepanel.getImageObject().getNumCols() + "   Row Res : "
                             + imagepanel.getImageObject().getNumRows());
          myGISOper.ComputeCTI(imagepanel.getImageObject());
          myShowImage.setImageObject(myGISOper.GetCTI());

          }

        }




    }//end else


  }

  public void SaveActivated (ActionEvent event) {

    System.out.println("test inside saveActivated =");
    String FileName=null;
    String DirName=null;
    //TODO!!
    String filename = "test.tif";
    try{
      ImageLoader.writeImage(filename, myGISOper.GetAspect());
    }catch(Exception e){
          System.out.println("Error: IO Exception");
    }
    /*mainFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));

    FileDialogIm2Learn dialog = new FileDialogIm2Learn(mainFrame, "Save A File",FileDialogIm2Learn.SAVE);

    try{
       //tiff file
       IIPImage myiip = new IIPImage();
       if(_edgeTypeStr.equalsIgnoreCase(_choiceText1)) {
         dialog.saveImage(myEdge.GetEdgeImageObject());
       }
       if(_edgeTypeStr.equalsIgnoreCase(_choiceText2)) {
         dialog.saveImage(myEdge.GetEdgeImageObject());
       }
       if(_edgeTypeStr.equalsIgnoreCase(_choiceText3)) {
         dialog.saveImage(myEdge.GetEdgeImageObject());
       }
       if(_edgeTypeStr.equalsIgnoreCase(_choiceText4)) {
         dialog.saveImage(myMorpho.GetEdgeMap());
       }
       if(_edgeTypeStr.equalsIgnoreCase(_choiceText5)) {
         dialog.saveImage(myMorpho.GetEdgeMap());
       }
       if(_edgeTypeStr.equalsIgnoreCase(_choiceText6)) {
         dialog.saveImage(mySpFilters.GetImageObject());
       }
       if(_edgeTypeStr.equalsIgnoreCase(_choiceText7)) {
         dialog.saveImage(mySpFilters.GetImageObject());
       }
       if(_edgeTypeStr.equalsIgnoreCase(_choiceText8)) {
         dialog.saveImage(myMorpho.GetImageObject());
       }
       if(_edgeTypeStr.equalsIgnoreCase(_choiceText9)) {
         dialog.saveImage(myMorpho.GetImageObject());
       }



    }  catch(Exception e){
        System.out.println("Error: IO Exception");
    }
*/
       // set the hour glass for slow saving
      // mainFrame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

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
      JMenu tools = new JMenu("Geo");

      JMenuItem compare = new JMenuItem(new AbstractAction("DEM Features") {
          public void actionPerformed(ActionEvent e) {
              if (!isVisible()) {
                  //LoadMask();
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
    URL url = null;
    
    if (url == null) {
        String file = menu.toLowerCase().replaceAll("[\\s\\?\\*]", "") + ".html"; 
        url = this.getClass().getResource("help/" + file);
    }
    
    return url;
  }


  // ------------------------------------------------------------------------
  //  HelpEntry implementation
  // ------------------------------------------------------------------------
/*  public HelpTopic[] getTopics() {
      HelpTopic topic = new HelpTopic("Image Compare");
      HelpTopic vis = new HelpTopic("Tools", topic);
      return new HelpTopic[]{vis};
  }

  public URL getHelp(String menu) {
      if (menu.equals("Image Compare")) {
          return getClass().getResource("help/ImageCompare.html");
      }
      return null;
  }
*/
}
