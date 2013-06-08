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
 * Created on Jul 14, 2005
 */
package edu.illinois.ncsa.isda.im2learn.ext.segment;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.net.URL;

import javax.swing.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageException;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.datatype.LimitValues;
import edu.illinois.ncsa.isda.im2learn.core.display.Im2LearnFrame;
import edu.illinois.ncsa.isda.im2learn.core.display.Im2LearnMenu;
import edu.illinois.ncsa.isda.im2learn.core.display.ImageAnnotation;
import edu.illinois.ncsa.isda.im2learn.core.display.ImagePanel;
import edu.illinois.ncsa.isda.im2learn.core.display.ImageUpdateEvent;


/**
 * @author Peter Ferak
 *
 * This class is the GUI for the bounding box computation tool. There are checkboxes for the options as to which statistical parts to display in the textArea.
 * The Width and Height values control the output, at this point only the drawing of the bounding boxes will be restricted by this, hence the algorithm still
 * finds all the areas which do not qualify. I have found this to be a major disadvantage because in bigger and complexer images this will waste a lot of computation
 * and memory. The Parameters will be set to 5 by default. This seems appropriate as it filters out noice but is still reasonably small.
 * I think the maximum Width and Height are selfexplanatory, same principle applies as for minimum.
 */
public class BoundBoxDialog extends Im2LearnFrame implements ActionListener, FocusListener, Im2LearnMenu, ImageAnnotation  {

    private JTextArea           txtMessage        	= new JTextArea(10, 40);
	private JCheckBox			opt1				= new JCheckBox("Print Number of Boxes");
	private JCheckBox			opt2				= new JCheckBox("Print ConnectAnalysis Stats");
	private JCheckBox			opt3				= new JCheckBox("Print Areas");
	private JCheckBox			opt4				= new JCheckBox("Print Blob Stats");
	private JCheckBox			opt5				= new JCheckBox("Print BoundBox Stats");
	private JTextField 			minArea				= new JTextField();
	private JTextField			minHeight			= new JTextField();
	private JTextField			minWidth			= new JTextField();
	private JTextField			maxArea				= new JTextField();
	private JTextField			maxHeight			= new JTextField();
	private JTextField			maxWidth			= new JTextField();
	private JToggleButton		btnShow				= new JToggleButton("Show");
	private ImagePanel 			imagepanel;
	private Blob 				_blob 				= null;
	private ConnectAnal			_connectAnalysis	= null;
	private static Log 			logger 				= LogFactory.getLog(BoundBoxDialog.class);

	public BoundBoxDialog() {
		super("Bounding Box Parameters");
		createUI();
	}
	
	/**
	 * This is one of the handy attributes of the Im2LearnFrame class (thank you Rob) which is called everytime the frame is displayed, I used it to set the
	 * values in the parameter boxes to my default values.
	 * The Area section is not editable and is merely the result of height * width, it didn't seem necessary to me to ever edit it, also the problem would be 
	 * that one would have to simultaniously change the height and width which leads to other problems that are redundant. If you think it would be helpful
	 * and decide you can solve the problem of reading someone's mind through a computer screen, thus knowing precisely what width or height the user has 
	 * in mind to go along with his area, feel free to change this.
	 */
    public void showing() {
 
    	minWidth.setText("5");
    	minHeight.setText("5");
    	maxWidth.setText(String.valueOf(LimitValues.MAX_INT));
    	maxHeight.setText(String.valueOf(LimitValues.MAX_INT));
    	
		minArea.setText(String.valueOf(Integer.parseInt(minHeight.getText()) * Integer.parseInt(minWidth.getText())));
		maxArea.setText(String.valueOf(Float.parseFloat(maxHeight.getText()) * Float.parseFloat(maxWidth.getText())));
    }
    
    public void closing() {
    	if(btnShow.isSelected())
    		btnShow.doClick();
    }
	
    
    /**
     * I don't think much needs to be added here, simply the method creating the user interface.
     */
	protected void createUI() {
		JPanel pnl, pnlbox, pnltmp;
		Box vbox, hbox;
		
        pnl = new JPanel(new GridLayout(2, 1));
        vbox = Box.createVerticalBox();
        
        pnlbox = new JPanel(new GridLayout(2,3));
        pnlbox.setBorder(BorderFactory.createTitledBorder("Features to Compute"));
        opt1.setSelected(true);
        pnlbox.add(opt1);
        pnlbox.add(opt2);
        pnlbox.add(opt3);
        pnlbox.add(opt4);
        pnlbox.add(opt5);
        vbox.add(pnlbox);
        vbox.add(Box.createGlue());
        pnl.add(vbox);
        
        
        hbox = Box.createHorizontalBox();
        
        pnlbox = new JPanel(new GridLayout(2,2));
        pnlbox.setBorder(BorderFactory.createTitledBorder("Width Constraint (in pixels)"));
        pnlbox.add(new JLabel("Min Width"));
        pnlbox.add(minWidth);
        minWidth.addFocusListener(this);
        pnlbox.add(new JLabel("Max Width"));
        pnlbox.add(maxWidth);
        maxWidth.addFocusListener(this);
        hbox.add(pnlbox);
        
        pnlbox = new JPanel(new GridLayout(2,2));
        pnlbox.setBorder(BorderFactory.createTitledBorder("Height Constraint (in pixels)"));
        pnlbox.add(new JLabel("Min Height"));
        pnlbox.add(minHeight);
        minHeight.addFocusListener(this);
        pnlbox.add(new JLabel("Max Height"));
        pnlbox.add(maxHeight);
        maxHeight.addFocusListener(this);
        hbox.add(pnlbox);
        pnl.add(hbox);
        
        pnlbox = new JPanel(new GridLayout(2,2));
        pnlbox.setBorder(BorderFactory.createTitledBorder("Area Constraint (in pixels)"));
        pnlbox.add(new JLabel("Min Area"));
        pnlbox.add(minArea);
        minArea.setEditable(false);
        pnlbox.add(new JLabel("Max Area"));
        pnlbox.add(maxArea);
        maxArea.setEditable(false);
        hbox.add(pnlbox);
        
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(pnl, BorderLayout.NORTH);
        
        txtMessage.setEditable(false);
        JScrollPane scrollpane = new JScrollPane(txtMessage, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        getContentPane().add(scrollpane, BorderLayout.CENTER);


        pnl = new JPanel();
        pnl.add(new JButton(new AbstractAction("Compute Bounding Box") {
            public void actionPerformed(ActionEvent e) {
                computeBox();
            }
        }));
        pnl.add(btnShow);
        btnShow.addActionListener(this);
        
        pnl.add(new JButton(new AbstractAction("Apply") {
            public void actionPerformed(ActionEvent e) {
                apply();
            }
        }));
        pnl.add(new JButton(new AbstractAction("Clear") {
            public void actionPerformed(ActionEvent e) {
                txtMessage.setText("");
                if(!btnShow.isEnabled())
                	btnShow.doClick();
            }
        }));
        pnl.add(new JButton(new AbstractAction("Done") {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        }));

        
        getContentPane().add(pnl, BorderLayout.SOUTH);

        pack();
	}


	/**
	 * This doesn't work because I'm not sure how to save the boxes which are drawn on a different level of the imagepanel. So all it does right now
	 * is save the image loaded in the imagepanel, not very useful, mind you.
	 */
	/*protected void saveFile() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save Resulting Image");
        try {
            String filename = fc.showSaveDialog();
            if (filename != null) {
            	saveImage = imagepanel.getImageObject();
            	saveImage.setProperty("Bounding Boxes", imagepanel.getAllAnnotationsImage());
                ImageLoader.writeImage(filename, "HDFLoader", saveImage);
            }
        } catch (IOException exc) {
            logger.debug("Error saving result.", exc);
        }
	}*/

	protected void apply() {
//            ImageObject imgobj = imagepanel.getImageObject();
//            if (imgobj == null) {
//                return;
//            }
//             imgobj.setProperty(PDFAnnotation.KEY, imagepanel.getAllAnnotationsImage());
//            //imgobj.setProperty("HasAnnotations", new Boolean(hasannotations));
//            imagepanel.repaint();
	}
	
	/**
	 * This method is called when the "Show" button is clicked. It calls the paint() method which displays the bounding boxes on the imagepanel.
	 * I would love to get credit for the idea with the Toggle Button, but I'm afraid I am too honest to do that, Rob came up with the useful idea of 
	 * changing it to a Toggle Button, upon clicking it the paint() method will draw the bounding boxes, upon releasing it they disappear. Ingenious indeed.
	 */
	protected void showBox(boolean show){
		
			if (show) {
				imagepanel.addAnnotationImage(this);
			} else {
				imagepanel.removeAnnotationImage(this);
			}
			imagepanel.repaint();
	}
	
	/**
	 * This method is called by the showBox() method and draws bounding box rectangles around the labeled areas.
	 * As I've already explained, the bounding boxes are filtered by the according parameters (pixel height, pixel width).
	 */
	public void paint(Graphics2D g, ImagePanel imagepanel) {
        if(_blob == null)
        	return;
        
        long numBox = _blob.getNumBoxLabels();
		// save the old graphics color
        Color old = g.getColor();
    	g.setColor(Color.red);
        for(int i=1;i<=numBox;i++){
        	
        	try {
             	if(((int)_blob.getBoundBox(i).ar.getWidth() > Integer.parseInt(minWidth.getText()) && (int)_blob.getBoundBox(i).ar.getWidth() < Integer.parseInt(maxWidth.getText()) )
						&& ( (int)_blob.getBoundBox(i).ar.getHeight() > Integer.parseInt(minHeight.getText()) && (int)_blob.getBoundBox(i).ar.getHeight() < Integer.parseInt(maxHeight.getText()) ) ) {
             			
        		            g.drawRect((int)_blob.getBoundBox(i).ar.getCol(),(int)_blob.getBoundBox(i).ar.getRow(), (int)_blob.getBoundBox(i).ar.getWidth(),(int)_blob.getBoundBox(i).ar.getHeight() );
        	    }
 			} catch (NumberFormatException e) {
 				logger.error("ERROR: The textboxes contain non-Integer characters.");
 				return;
 			}
        }
        //         
        // set graphics color back to original
        g.setColor(old);
    }

	/**
	 * This method calls the doer BoundBox, which calculates the bounding boxes, and prints the desired statistics.
	 */

	protected void computeBox() {

		BoundBox myBoundBox = new BoundBox();
		try
		{
			_blob = myBoundBox.computeBoundingBox(imagepanel.getImageObject(), minHeight.getText(), minWidth.getText(), minArea.getText());
			logger.debug("TEST inside computeBox");
			_connectAnalysis = myBoundBox.getConnectAnalysis();
		
		} catch (ImageException e){ 
			e.printStackTrace();
		}
		
		if(opt1.isSelected()) 
			txtMessage.append("Number of Boxes: " + _connectAnalysis.getNFoundS() + "\n" + "\n");
		
		if(opt2.isSelected()) {

			
				txtMessage.append("ConnectAnal: Variables \n"  + "\n");
		
				if (_connectAnalysis.getImLabels() != null) {// labeled index
					txtMessage.append("Label Image:" + "\n");
					_connectAnalysis.getImLabels().toString();
				}
		
				txtMessage.append("NFoundS=" + _connectAnalysis.getNFoundS() + "\n");
		
				if (_connectAnalysis.getAreaS() != null) { // size of labeled segment
					txtMessage.append("Area[1]=" + _connectAnalysis.getAreaS()[1] + "\n");
				}
		
				if (_connectAnalysis.getMeanBinValS() != null) { // value of a binary labeled segment
					txtMessage.append("MeanBinValS[1]=" + _connectAnalysis.getMeanBinValS()[1] + "\n");
				}
		
				if (_connectAnalysis.getMeanValS() != null) { // sample mean of labeled segment
					txtMessage.append("MeanValS[1]=" + _connectAnalysis.getMeanValS()[1] + "\n");
				}
		
				if (_connectAnalysis.getStDevValS() != null) { // sample stdev of labeled segment
					txtMessage.append("StDevValS[1]=" + _connectAnalysis.getStDevValS()[1] + "\n");
				}
		
				if (_connectAnalysis.getMinS() != null) { // min value of labeled segment
					txtMessage.append("MinS[1]=" + _connectAnalysis.getMinS()[1] + "\n");
				}
		
				if (_connectAnalysis.getMaxS() != null) { // max value of labeled segment
					txtMessage.append("MaxS[1]=" + _connectAnalysis.getMaxS()[1] + "\n");
				}
		
				txtMessage.append("_NumCols =" + _connectAnalysis.getNumCols() + ", _NumRows =" + _connectAnalysis.getNumRows()
						+ ", Invalid = " + _connectAnalysis.getInvalid() + "\n" + "\n");
		}
		
		if(opt3.isSelected()) {
			txtMessage.append("ConnectAnal: AreaS" + "\n" + "\n");
			
			if (_connectAnalysis.getAreaS() == null)
				return;
			
			for (int idx = 1; idx < _connectAnalysis.getNFoundS() + 1; idx++) {
				txtMessage.append("AreaS[" + idx + "]=" + _connectAnalysis.getAreaS()[idx] + "\n");
			}
			
			
		}
		
		if(opt4.isSelected()) {
		      if(_blob.getBlobDes() == null){
		      	logger.debug( "Error: no blob descriptiors \n");
		        txtMessage.append("Error: no blob descriptiors \n");
		        return;
		      }
		      int index;
		      for(index=1;index<_blob.getNumBoxLabels()+1;index++){
		        txtMessage.append("idx="+index+", "+_blob.getBlobDes()[index].BlobStats2String() + "\n");
		      }
		}
		
		if(opt5.isSelected()) {
		      if(_blob.getBoundBox() == null){
		        logger.debug( "Error: no blob descriptors \n");
		        txtMessage.append("Error: no blob descriptors \n");
		        return;
		      }
		      int index;
		      for(index=1;index<_blob.getNumBoxLabels()+1;index++){
		        txtMessage.append("idx="+index+", "+_blob.getBoundBox()[ index ].blobBound2String() + "\n");
		      }
		}
			
			
	}

	/**
	 * Quite useless because all the actionPerformed events are already defined with the buttons, except the Show button because I needed to have the ability
	 * to induce a click which is done in the focusGained method.
	 */
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == btnShow)
			 showBox(btnShow.isSelected());
	}

	/**
	 * When the focus in the Width or Height Textboxes is lost the area automatically updates.
	 */
	public void focusLost(FocusEvent e) {
		
		if(e.getSource() == minHeight || e.getSource() == maxHeight || e.getSource() == minWidth || e.getSource() == maxWidth) {
			  minArea.setText(String.valueOf(Integer.parseInt(minHeight.getText()) * Integer.parseInt(minWidth.getText())));
			  maxArea.setText(String.valueOf(Float.parseFloat(maxHeight.getText()) * Float.parseFloat(maxWidth.getText())));
		}
	}
	
	/**
	 * I find this to be very useful and quite a smart function. When any of the textboxes is clicked the and the bounding boxes are still drawn
	 * the ToggleButton will release instantly and the drawing ceases to exist.
	 */
	public void focusGained(FocusEvent e) {
		if(btnShow.isSelected())
			btnShow.doClick();
		
	}

    public void setBlob(Blob myblob) {
        this._blob = myblob;
    }

    public Blob getBlob() {
        return this._blob;
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

        JMenuItem seg2DSuper = new JMenuItem(new AbstractAction("Bounding Box Tool") {
            public void actionPerformed(ActionEvent e) {
                if (!isVisible()) {
                    Window win = SwingUtilities.getWindowAncestor(imagepanel);
                    setLocationRelativeTo(win);
                    setVisible(true);
                }
                toFront();
            }
        });
        tools.add(seg2DSuper);

        return new JMenuItem[]{tools};
    }

    public void imageUpdated(ImageUpdateEvent event) {
    }
    
    public URL getHelp(String menu) {
        if (menu.equals("Bounding Box")) {
            return getClass().getResource("help/boundbox.html");
        }
        return null;
    }
	
}
