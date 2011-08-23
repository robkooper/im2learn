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
package edu.illinois.ncsa.isda.imagetools.ext.vis;

/*
 * MagWindow.java
 *
 *
 */


/**
 *
 * @author Sang-Chul Lee
 * @version 1.0
 */


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageException;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.datatype.SubArea;
import edu.illinois.ncsa.isda.imagetools.core.display.Im2LearnFrame;
import edu.illinois.ncsa.isda.imagetools.core.display.Im2LearnMainFrame;
import edu.illinois.ncsa.isda.imagetools.core.display.Im2LearnMenu;
import edu.illinois.ncsa.isda.imagetools.core.display.ImageMarker;
import edu.illinois.ncsa.isda.imagetools.core.display.ImagePanel;
import edu.illinois.ncsa.isda.imagetools.core.display.ImageUpdateEvent;
import edu.illinois.ncsa.isda.imagetools.core.display.ImageUpdateListener;
import edu.illinois.ncsa.isda.imagetools.core.io.ImageLoader;
import edu.illinois.ncsa.isda.imagetools.ext.misc.ImageFrame;
import edu.illinois.ncsa.isda.imagetools.ext.panel.ZoomDialog;




public class MagWindow extends Im2LearnFrame implements ActionListener, KeyListener, Im2LearnMenu {
	MI2LearnSubFrame _subFrame = null;
	private ImageObject _sourceImObj = null; 
	protected ImageObject _subImageObject = null;
	protected ImagePanel _imPanel = null;
	private Rectangle _subarea;
	JButton _bt_done, _bt_extract;

	JTextField _row, _col, _width, _height,_from, _to, _zoom;
	SubArea _window = new SubArea();
	ImageMarker magMarker1, magMarker2;
	
	Point magLoc = new Point();
	int magW, magH;
	boolean _magActive = true;
	boolean selectionEnabled = true, toolEnabled = true;
	
	JRadioButton _rb_nav, _rb_sel;

	ImageLoader loader = new ImageLoader();
	boolean _loadFromDisk = false;
	boolean _getRenderedImage = true;
	
  public MagWindow() {
	  super("Magnification Dialog");
	  setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	  createUI();	
  }
  
  public MagWindow(boolean enableSelection) {
	  super("Magnification Dialog");
	  selectionEnabled = enableSelection;
	  setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	  createUI();	
  }
  
  public MagWindow(boolean enableSelection, boolean enableTools) {
	  super("Magnification Dialog");
	  selectionEnabled = enableSelection;
	  toolEnabled = enableTools;
	  setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	  createUI();	
  }
  
  public MagWindow(String higresFilename) {
	  super("Magnification Dialog");
	  setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	  createUI();	
  }
  
  
  private void createUI() {
	  
//	  if(_subarea.height < 1 || _subarea.width < 1) {
//		  _subarea.setBounds(0,0,_sourceImObj.getNumRows(),_sourceImObj.getNumRows());
//	  }
//	  
    try {
      jbInit();
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    //   makeWindow();
  _loadFromDisk = false;
  }

 public void keyPressed(KeyEvent e) {
   if(e.getKeyCode() == KeyEvent.VK_ENTER){
  
	  //  updateEvent(false);
   }
 }
 public void keyReleased(KeyEvent e) {
 }
 public void keyTyped(KeyEvent e) {
 }

 public void showing() {
	 imageUpdated(new ImageUpdateEvent(this, ImageUpdateEvent.NEW_IMAGE, null));
    
 }
 
 public void hiding() {
 }
 
 public void closing() {
 }
 
 public void actionPerformed(ActionEvent e) {
	 if(e == null) {
		 return;
	 }
    Object obj = e.getSource();
  
    if(obj == _bt_done) {
    
    	_row.setText("");
    	_col.setText("");
    	_width.setText("");
    	_height.setText("");
    	_loadFromDisk = false;
    	
    	_imPanel.setSelectionAllowed(true);
    	if(_subFrame != null) {
    		_subFrame.setVisible(false);
    		//_subFrame.dispose();
    	}
    	_rb_nav.setSelected(false);
    	_rb_sel.setSelected(true);
    	    	
    	setVisible(false);
    	
    	//dispose();
    }
    else if(obj == _bt_extract) {
    	try {
//    		if(magMarker1 != null) {
//        		_imPanel.removeAnnotationImage(magMarker1);
//        	}
//        	if(magMarker2 != null) {
//        		_imPanel.removeAnnotationImage(magMarker2);
//        	}
    		if(_subFrame == null) {
    			launchSubArea();
    			_rb_nav.setSelected(true);
    			_rb_sel.setSelected(false);
    		}
    		else {
    			_rb_nav.setSelected(true);
    			_rb_sel.setSelected(false);
    			navigate();
    			_subFrame.setVisible(true);
    	    	_imPanel.setSelectionAllowed(false);
    		}
    		
    	} catch (ImageException e1) {
    		// TODO Auto-generated catch block
    		e1.printStackTrace();
    	}
    }
    else if(obj == _rb_nav) {
    //_magActive = true;
    	if(_subFrame == null) {
    		try {
				launchSubArea();
			} catch (ImageException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
    	}
    	else {
    		navigate();
    	}
    	_subFrame.setVisible(true);
    	_imPanel.setSelectionAllowed(false);
    }
    else if(obj == _rb_sel) {
    //	_magActive = false;
    	_subFrame.setVisible(false);
    	_imPanel.setSelectionAllowed(true);
    }
    
}
 

 private void launchSubArea() throws ImageException {
	 if(_col.getText().equalsIgnoreCase("") ||
		_row.getText().equalsIgnoreCase("") ||
		_width.getText().equalsIgnoreCase("") ||
		_height.getText().equalsIgnoreCase("")) {
		 JOptionPane.showMessageDialog(this, "Select subarea to magnify", "Error", JOptionPane.ERROR_MESSAGE, null);
		 return;
	 }
	 
	 if(_sourceImObj.getProperty("OrgH") != null && _sourceImObj.getProperty("OrgW") != null) { 
		 if(((Integer)_sourceImObj.getProperty("OrgH")).intValue() != _sourceImObj.getNumRows() ||
				 ((Integer)_sourceImObj.getProperty("OrgW")).intValue() != _sourceImObj.getNumCols()) {
			_loadFromDisk = true;
		 } 
	 }
	 
   int from,to;
   try {

     from = Integer.parseInt(_from.getText());
     to = Integer.parseInt(_to.getText());
     if(_subarea == null) {
    	_subarea = new Rectangle(); 
     }
     
     
     if(!_loadFromDisk) {
    	 _subarea.setBounds(Integer.parseInt(_col.getText()), 
    			 Integer.parseInt(_row.getText()),
    			 Integer.parseInt(_width.getText()),
    			 Integer.parseInt(_height.getText()));
     }
	 else {
		 int ow = ((Integer)_sourceImObj.getProperty("OrgW")).intValue();
		 int oh = ((Integer)_sourceImObj.getProperty("OrgH")).intValue();
		 float wr = (float)ow/(float)_sourceImObj.getNumCols();
		 float hr = (float)oh/(float)_sourceImObj.getNumRows();
	
		 _subarea.setBounds((int)(Integer.parseInt(_col.getText())*wr), 
				 (int)(Integer.parseInt(_row.getText())*hr),
				 (int)(Integer.parseInt(_width.getText())*wr),
				 (int)(Integer.parseInt(_height.getText())*hr));
	 }
     
     
		  
   }
   catch(Exception ee) {
	   ee.printStackTrace();
	   System.err.println("Error in input values");
     return;
   }

   
  // System.out.println(_sourceImObj.getFileName());
   
  // System.out.println(_sourceImObj.getProperty("OrgH")+ " -- "+ _sourceImObj.getProperty("OrgW"));
  
   
  
      
   int[] band = new int[to-from+1];
   for(int i=0; i<band.length; i++) {
     band[i] = from+i;
   }
   if(_subarea.x == 0 && _subarea.y == 0 &&
		   _subarea.width == this._sourceImObj.getNumCols() &&
		   _subarea.height == this._sourceImObj.getNumRows()) {
	   
	   if(!_loadFromDisk) {
		   _subImageObject = _sourceImObj.extractBand(band);
	   }
	   else {
		   try 	{
			   synchronized(loader) {
				   _subImageObject = loader.readImage(_sourceImObj.getFileName());
			   }
		   } catch (IOException e) {
			   //	 TODO Auto-generated catch block
			   e.printStackTrace();	
		   }
	   }
   }
   else {
	   if(!_loadFromDisk) {
		   try{
		   _subImageObject = _sourceImObj.crop(new SubArea(_subarea.x, _subarea.y, _subarea.width, _subarea.height)).extractBand(band);
		   }
		   catch(ArrayIndexOutOfBoundsException eee) {
			   JOptionPane.showMessageDialog(this, "Image has been changed. Select the magnifying area first.");
		   }
	   }
	   else {
		   try {
			   synchronized(loader) {
				   _subImageObject = loader.readImage(_sourceImObj.getFileName(),new SubArea(_subarea), 1);
			   }
		} 			   catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   }
   }
   
   Point p = this.getLocation();
   if(_subFrame == null) {
	   _subFrame = new MI2LearnSubFrame(_subImageObject);
	 
	  // _subFrame.getImagePanel().setZoomFactor(1);
	   _subFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
   } else {
	   _subFrame.dispose();
	   _subFrame = new MI2LearnSubFrame(_subImageObject);
	  // 
	   _subFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
   }
   
  // _subFrame.getImagePanel().setZoomFactor(1);
   _subFrame.setLocation(p.x, p.y+this.getHeight());
  // _subFrame.setResizable(false);
  // _subFrame.setPreferredSize(new Dimension(100,100));
 
   _imPanel.setSelectionAllowed(false);
   
  
    
 }
 


 private void setBand(int from, int to) {
   _from.setText(String.valueOf(from));
   _to.setText(String.valueOf(to));
 }


 
 private void jbInit() throws Exception {
    JPanel controlPane = new JPanel(new BorderLayout());
    JPanel coordPane = new JPanel(new GridLayout(2,4));
    JPanel buttonPane = new JPanel();
    JPanel propertyPane = new JPanel(new GridLayout(1,5));
    JPanel zoomPane = new JPanel();
    
    JLabel Col = new JLabel("Col");
    JLabel Height = new JLabel("Height");
    JLabel Width = new JLabel("Width");
    JLabel Row = new JLabel("Row");
 
    JLabel band = new JLabel("Band : ");
    JLabel from = new JLabel("from");
    JLabel to = new JLabel("to");

    _row = new JTextField();
    _col = new JTextField();
    _width = new JTextField();
    _height = new JTextField();

    _from = new JTextField(5);
    _to = new JTextField(5);

    _zoom = new JTextField("   1.0");
    _zoom.setEditable(false);
    
    _row.addKeyListener(this);
    _col.addKeyListener(this);
    
    _width.addKeyListener(this);
    _height.addKeyListener(this);
    _from.addKeyListener(this);
    _to.addKeyListener(this);

    _bt_done = new JButton("Done");
    _bt_extract  = new JButton("Show");
       
    _bt_done.addActionListener(this);
    _bt_extract.addActionListener(this);
    
    to.setHorizontalAlignment(SwingConstants.CENTER);
    from.setHorizontalAlignment(SwingConstants.CENTER);
    band.setHorizontalAlignment(SwingConstants.RIGHT);
    Row.setHorizontalAlignment(SwingConstants.CENTER);
    Col.setHorizontalAlignment(SwingConstants.CENTER);
    Width.setHorizontalAlignment(SwingConstants.CENTER);
    Height.setHorizontalAlignment(SwingConstants.CENTER);

    coordPane.setBorder(BorderFactory.createLoweredBevelBorder());
    propertyPane.setBorder(BorderFactory.createLoweredBevelBorder());
    zoomPane.setBorder(BorderFactory.createLoweredBevelBorder());
    buttonPane.setBorder(BorderFactory.createLoweredBevelBorder());

    coordPane.add(Col, null);
    coordPane.add(Row, null);
    coordPane.add(Width, null);
    coordPane.add(Height, null);
    coordPane.add(_col, null);
    coordPane.add(_row, null);
    coordPane.add(_width, null);
    coordPane.add(_height, null);

    propertyPane.add(band, null);
    propertyPane.add(from, null);
    propertyPane.add(_from, null);
    propertyPane.add(to, null);
    propertyPane.add(_to, null);
  
    zoomPane.add(new JLabel("Zoom Factor: "), null);
    zoomPane.add(_zoom, null);
    
    
    ButtonGroup group = new ButtonGroup();
    _rb_nav = new JRadioButton("Navigate");
       
    group.add(_rb_nav);
    zoomPane.add(_rb_nav);

    _rb_sel = new JRadioButton("Select");
    _rb_sel.setEnabled(selectionEnabled);
    _rb_sel.setSelected(true);
    
    group.add(_rb_sel);
    zoomPane.add(_rb_sel);
            
    _rb_nav.addActionListener(this);
    _rb_sel.addActionListener(this);
    
    controlPane.add(propertyPane, BorderLayout.NORTH);
    controlPane.add(zoomPane, BorderLayout.CENTER);
    controlPane.add(buttonPane, BorderLayout.SOUTH);

    buttonPane.add(_bt_extract, null);
    buttonPane.add(_bt_done, null);

    this.getContentPane().add(coordPane, BorderLayout.NORTH);
    this.getContentPane().add(controlPane, BorderLayout.SOUTH);
    
    this.pack();
    
	
  }
 
 // ------------------------------------------------------------------------
 // Im2LearnMenu implementation
 // ------------------------------------------------------------------------
 public void setImagePanel(ImagePanel imagepanel) {
     this._imPanel = imagepanel;
 }

 public JMenuItem[] getPanelMenuItems() {
	 JMenuItem imageextract = new JMenuItem(new AbstractAction("Magnifier") {
         public void actionPerformed(ActionEvent e) {
        	 
        	 if(_getRenderedImage) {
				 try {
					_sourceImObj = ImageObject.getImageObject(_imPanel.getImage());
					_sourceImObj.setProperty(ImageObject.FILENAME, _imPanel.getImageObject().getFileName());
				} catch (ImageException ee) {
					// TODO Auto-generated catch block
					ee.printStackTrace();
				}				 
			 }
			 else {
				 _sourceImObj = _imPanel.getImageObject();				 
			 }
        	 
			 setBand(0, _sourceImObj.getNumBands()-1);
			 
			 _subarea = _imPanel.getSelection();
			 if(_subarea != null) {
				 _row.setText(String.valueOf(_subarea.y));
				 _col.setText(String.valueOf(_subarea.x));
				 _width.setText(String.valueOf(_subarea.width));
				 _height.setText(String.valueOf(_subarea.height));
			 }
			 
			 setLocationRelativeTo(getOwner());
			 
			 Point p = _imPanel.getParent().getLocationOnScreen();//_imPanel.getLocationOnScreen();
			 
			 setLocation((int)p.getX()+_imPanel.getParent().getWidth()+5,0);
			 setVisible(true);
         }
     });
	 return new JMenuItem[]{imageextract};
 }

 public JMenuItem[] getMainMenuItems() {
     JMenu tools = new JMenu("Visualization");

     JMenuItem imageextract = new JMenuItem(new AbstractAction("Magnifier") {
         public void actionPerformed(ActionEvent e) {
        	 
        	 if(_getRenderedImage) {
				 try {
					_sourceImObj = ImageObject.getImageObject(_imPanel.getImage());
					_sourceImObj.setProperty(ImageObject.FILENAME, _imPanel.getImageObject().getFileName());
				} catch (ImageException ee) {
					// TODO Auto-generated catch block
					ee.printStackTrace();
				}				 
			 }
			 else {
				 _sourceImObj = _imPanel.getImageObject();				 
			 }
        	 
        	 
        	 setBand(0, _sourceImObj.getNumBands()-1);
			 
			 _subarea = _imPanel.getSelection();
			 if(_subarea != null) {
				 _row.setText(String.valueOf(_subarea.y));
				 _col.setText(String.valueOf(_subarea.x));
				 _width.setText(String.valueOf(_subarea.width));
				 _height.setText(String.valueOf(_subarea.height));
			 }
			 setLocationRelativeTo(getOwner());
			 setVisible(true);
         }
     });
     tools.add(imageextract);
     return new JMenuItem[]{tools};
 }

 
  void navigate() {
	 try {
		 if(_imPanel.isSelectionAllowed())
			 _imPanel.setSelectionAllowed(false);
		 
		 if(_sourceImObj.getProperty("OrgH") != null && _sourceImObj.getProperty("OrgW") != null) { 
			 if(((Integer)_sourceImObj.getProperty("OrgH")).intValue() != _sourceImObj.getNumRows() ||
					 ((Integer)_sourceImObj.getProperty("OrgW")).intValue() != _sourceImObj.getNumCols()) {
				_loadFromDisk = true;
			 } 
		 }
		 
		 if(!_loadFromDisk) {
			 magW = Integer.parseInt(_width.getText());
			 magH = Integer.parseInt(_height.getText());
			 magLoc.x = Integer.parseInt(_col.getText());
			 magLoc.y = Integer.parseInt(_row.getText());
			  
			 _window.setSubArea(magLoc.y, magLoc.x, magH, magW, true);
				
		 }
		 else {
			 int ow = ((Integer)_sourceImObj.getProperty("OrgW")).intValue();
			 int oh = ((Integer)_sourceImObj.getProperty("OrgH")).intValue();
			 
			 float wr = (float)ow/(float)_sourceImObj.getNumCols();
			 float hr = (float)oh/(float)_sourceImObj.getNumRows();
			 
			 magW = Integer.parseInt(_width.getText());
			 magH = Integer.parseInt(_height.getText());
			 magLoc.x = Integer.parseInt(_col.getText());
			 magLoc.y = Integer.parseInt(_row.getText());
			 
			 _window.setSubArea((int)(magLoc.y*hr), (int)(magLoc.x*wr), 
					 			(int)(magH*hr),(int)(magW*wr), true);
		 }
		 
		 //_window.setSubArea(magLoc.y , magLoc.x , magH,magW, true);
		 
		 int from = Integer.parseInt(_from.getText());
		 int to = Integer.parseInt(_to.getText());
		 
		 if(from < 0 || 
			from > _sourceImObj.getNumBands()-1 ||
			from > to  ||
			to > _sourceImObj.getNumBands()-1) {

			 JOptionPane.showMessageDialog(this, "Invalid band range", "Error", JOptionPane.ERROR_MESSAGE);
			 return;
		 }
		 
		 
		 int[] band = new int[to-from+1];
		 for(int i=0; i<band.length; i++) {
			 band[i] = from+i;
		 }
		   
		 
		 if(!_loadFromDisk) {
			 _subImageObject = _sourceImObj.crop(_window).extractBand(band);			 
		 }
		 else {
			 try {
				 synchronized(loader) {
				 _subImageObject = loader.readImage(_sourceImObj.getFileName(),_window, 1).extractBand(band);
				 }
				// System.out.println(_subImageObject.getSize());
			 }	 catch (IOException e) {
				 // TODO Auto-generated catch block
				 e.printStackTrace();
			 }
		 }
		


		 _subFrame.getImagePanel().setImageObject(_subImageObject);
	
		 
		 if(magMarker1 == null) {
			 magMarker1 = new ImageMarker(0,0,10,10, ImageMarker.RECTANGLE);
			 magMarker1.setColor(Color.BLUE);
			 _imPanel.addAnnotationImage(magMarker1);
		 }
		 if(magMarker2 == null) {
			 magMarker2 = new ImageMarker(0,0,1,1, ImageMarker.CROSS);
			 magMarker2.setColor(Color.BLUE);
			 _imPanel.addAnnotationImage(magMarker2);
		 }
		 
		   
		 
//		 magMarker1.setSubArea(magLoc.y +(int)((double)magH/(double)2), 
//				 			   magLoc.x + (int)((double)magW/(double)2), magH, magW, true);
		 magMarker1.setSubArea(magLoc.y, magLoc.x, magH, magW, true);
		 magMarker1.setVisible(true);
		 magMarker2.setSubArea(magLoc.y + (int)((double)magH/(double)2), 
				 			   magLoc.x + (int)((double)magW/(double)2), 10, 10, true);
		 magMarker2.setVisible(true);

		 // _imPanel.setSelection(new Rectangle(_window.x, _window.y, _window.width, _window.height));
		 
		

		 _imPanel.setSelectionAllowed(true);
		 _imPanel.setSelection(new Rectangle(magLoc.x, magLoc.y, magW, magH));
		 _imPanel.setSelectionAllowed(false);
		
		
		 
	 } catch (ImageException e) {
		// TODO Auto-generated catch block
		//e.printStackTrace();
	}
 }
 
 public void imageUpdated(ImageUpdateEvent event) {
//	 System.out.println(event.getId());
	 if (isVisible()) {
		 
		 if ((event.getId() == ImageUpdateEvent.NEW_IMAGE) ||
			 (event.getId() == ImageUpdateEvent.CHANGE_GAMMA) ||
			 (event.getId() == ImageUpdateEvent.CHANGE_REDBAND) ||
			 (event.getId() == ImageUpdateEvent.CHANGE_BLUEBAND) ||
			 (event.getId() == ImageUpdateEvent.CHANGE_GREENBAND) ||
			 (event.getId() == ImageUpdateEvent.CHANGE_GRAYBAND) ||
			 (event.getId() == ImageUpdateEvent.CHANGE_GRAYSCALE) ||
			 (event.getId() == ImageUpdateEvent.CHANGE_RGBBAND) ||
			 (event.getId() == ImageUpdateEvent.CHANGE_CROP) 
			 ) {
			 if(_getRenderedImage ) {
				 try {
					_sourceImObj = ImageObject.getImageObject(_imPanel.getImage());
					_sourceImObj.setProperty(ImageObject.FILENAME, _imPanel.getImageObject().getFileName());
				} catch (ImageException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				 
			 }
			 else {
				 _sourceImObj = _imPanel.getImageObject();				 
			 }
 

			 ImageObject headerIm;
			try {
				String fn = _sourceImObj.getFileName();
				if(fn != null) {
					headerIm = ImageLoader.readImageHeader(fn);
					_sourceImObj.setProperty("OrgH",headerIm.getNumRows());
					_sourceImObj.setProperty("OrgW",headerIm.getNumCols());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		 }
		 
		 else if ((event.getId() == ImageUpdateEvent.CHANGE_SELECTION)) {
			 if(!_magActive && !_imPanel.isSelectionAllowed())
				 _imPanel.setSelectionAllowed(true);
			 
			// setBand(0, _sourceImObj.getNumBands()-1);
			 _subarea = _imPanel.getSelection();
			 
			 if(_subarea != null /*&& _magActive == true*/) {
				 _row.setText(String.valueOf(_subarea.y));
				 _col.setText(String.valueOf(_subarea.x));
				 _width.setText(String.valueOf(_subarea.width));
				 _height.setText(String.valueOf(_subarea.height));
			 } 
		 }
		 
		 else if ((event.getId() == ImageUpdateEvent.MOUSE_CLICKED)) {
			 
			 if(_subFrame != null && _subFrame.isVisible() && _magActive) {
				 magLoc = _imPanel.getImageLocationClicked();
				 
				 magW = Integer.parseInt(_width.getText());
				 magH = Integer.parseInt(_height.getText());
				 					 
				 
				 magLoc.x -= (int)((double)magW/(double)2);
				 magLoc.y -= (int)((double)magH/(double)2);
				 
				 if(magLoc.x + magW > _sourceImObj.getWidth()) {
					 magLoc.x = _sourceImObj.getWidth() - magW;
				 }
				 if(magLoc.y + magH > _sourceImObj.getHeight()) {
					 magLoc.y = _sourceImObj.getHeight() - magH;
				 }
				 
				 if(magLoc.x < 0) {
					 magLoc.x = 0;
				 }
				 if(magLoc.y < 0) {
					 magLoc.y = 0;
				 } 
				 
				 _col.setText(String.valueOf(magLoc.x));
				 _row.setText(String.valueOf(magLoc.y));
				 
				 navigate();
			 }
		 }
	 }
		 
		 
 }
	 
 
 
 public URL getHelp(String menu) {
     return getClass().getResource("help/magnifier.html");
 }
 

 
 public class MI2LearnSubFrame extends Im2LearnFrame implements ImageUpdateListener {
	 ImagePanel imagepanel;
	 /**
	  * Create the MainFrame with the menus
	  */
	 public MI2LearnSubFrame(ImageObject image) {
		 super("Magnifier");
		 this.setTitle("Magnifier");
		 imagepanel = new ImagePanel();
		 imagepanel.addImageUpdateListener(this);
		 imagepanel.setAutozoom(true);
	        
		 imagepanel.setImageObject(image);
		 this.getContentPane().add(imagepanel);
		 this.pack();
		 addMenus();
		 
		 setVisible(true);
	 }
	    
	 public void addMenus() {
		 imagepanel.addMenu(new ZoomDialog());
		 if(toolEnabled)
			 imagepanel.addMenu(new ImageExtractDialog());
	 }

	 public ImagePanel getImagePanel() {
		 return imagepanel;
	 }
	 
	 public void imageUpdated(ImageUpdateEvent event) {
		if(event.getId() == ImageUpdateEvent.CHANGE_ZOOMFACTOR) {
			DecimalFormat myFormatter = new DecimalFormat("###.##");
			_zoom.setText(myFormatter.format(imagepanel.getZoomFactor()));
		}
	 }
	 
	 public void showing() {
		 this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);	
		 _magActive = true;
	 }
	 
	 public void closing() {
		 _magActive = false;
		 if(magMarker1 != null) {
			 _imPanel.removeAnnotationImage(magMarker1);	
		 }
		 if(magMarker2 != null) {
			 _imPanel.removeAnnotationImage(magMarker2);
		 }
		 
		 magMarker1 = null;
		 magMarker2 = null;
		 _imPanel.repaint();

	 }

	
	 
	}

 
}

