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
package edu.illinois.ncsa.isda.imagetools.ext.panel;


import javax.swing.*;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageException;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.display.Im2LearnMenu;
import edu.illinois.ncsa.isda.imagetools.core.display.ImageAnnotation;
import edu.illinois.ncsa.isda.imagetools.core.display.ImageMarker;
import edu.illinois.ncsa.isda.imagetools.core.display.ImagePanel;
import edu.illinois.ncsa.isda.imagetools.core.display.ImageUpdateEvent;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Create a submenu to show the different annotations. Allow the user to place
 * annotations as well.
 *
 * @author Rob Kooper
 * @version $Revision: 1.11 $ - $Date: 2007-07-13 21:54:32 $
 */
public class AnnotationDialog implements Im2LearnMenu {
    private ImagePanel imagepanel;
    private int annotationtype = ImageMarker.CROSS;
 //   private BasicStroke annotationstyle = ImageMarker.SOLID;
    private boolean annotationselection = true, labelselection = true;
    private ArrayList annotations = new ArrayList();
    JTextField labelfd;
    // ------------------------------------------------------------------------
    // Im2LearnMenu implementation
    // ------------------------------------------------------------------------
    public void setImagePanel(ImagePanel imagepanel) {
        this.imagepanel = imagepanel;
    }

    public JMenuItem[] getPanelMenuItems() {
        JMenu annotation = new JMenu("Annotation");
        JRadioButtonMenuItem radio;
        ButtonGroup group;
    
        
        JMenuItem menu = new JMenuItem(new AbstractAction("Make Annotation") {
            public void actionPerformed(ActionEvent e) {
                Rectangle rect = null;
                if (annotationselection) {
                    rect = imagepanel.getSelection();
                    if ((rect != null) ) {
                        rect.x += (rect.width / 2);
                        rect.y += (rect.height / 2);
                    }
                } else {
                    Point p = imagepanel.getImageLocationClicked();
                    if (p != null) {
                        rect = new Rectangle(p.x, p.y, 5, 5);
                    }
                }
                if (rect != null) {
                    ImageMarker marker = new ImageMarker();
                    marker.setRect(rect);
                    marker.setType(annotationtype);
                    if(labelselection) {
	                    String label = labelfd.getText();
	                    if(label != null) {
	                    	marker.setLabel(label);
	                    }
                    }
               //     marker.setStroke(annotationstyle);
                    marker.setVisible(true);
                    imagepanel.addAnnotationImage(marker);
                    annotations.add(marker);
                }
            }
        });
        annotation.add(menu);

        labelfd = new JTextField(8);
        labelfd.addMouseListener(new MouseListener(){
        	public void actionPerformed(ActionEvent e) {}

			public void mouseClicked(MouseEvent arg0) {
				labelfd.setText("");
			}

			public void mouseEntered(MouseEvent arg0) {}

			public void mouseExited(MouseEvent arg0) {}

			public void mousePressed(MouseEvent arg0) {}

			public void mouseReleased(MouseEvent arg0) {}
        });
        annotation.add(labelfd);
        
        labelfd.setVisible(labelselection);
        
        menu = new JMenuItem(new AbstractAction("Clear Annotations") {
            public void actionPerformed(ActionEvent e) {
                for (Iterator iter = annotations.iterator(); iter.hasNext();) {
                    imagepanel.removeAnnotationImage((ImageMarker) iter.next());
                }
                annotations.clear();
            }
        });
        annotation.add(menu);

        // Type of annotation
        annotation.addSeparator();
        group = new ButtonGroup();
        radio = new JRadioButtonMenuItem(new AbstractAction("Cross") {
            public void actionPerformed(ActionEvent e) {
                annotationtype = ImageMarker.CROSS;
            }
        });
        radio.setSelected(true);
        group.add(radio);
        annotation.add(radio);


        radio = new JRadioButtonMenuItem(new AbstractAction("Circle") {
            public void actionPerformed(ActionEvent e) {
                annotationtype = ImageMarker.CIRCLE;
            }
        });
        group.add(radio);
        annotation.add(radio);

        radio = new JRadioButtonMenuItem(new AbstractAction("Rectangle") {
            public void actionPerformed(ActionEvent e) {
                annotationtype = ImageMarker.RECTANGLE;
            }
        });
        group.add(radio);
        annotation.add(radio);

        radio = new JRadioButtonMenuItem(new AbstractAction("Line(down)") {
            public void actionPerformed(ActionEvent e) {
                annotationtype = ImageMarker.LINEDOWN;
            }
        });
        group.add(radio);
        annotation.add(radio);

        radio = new JRadioButtonMenuItem(new AbstractAction("Line(up)") {
            public void actionPerformed(ActionEvent e) {
                annotationtype = ImageMarker.LINEUP;
            }
        });
        group.add(radio);
        annotation.add(radio);

        radio = new JRadioButtonMenuItem(new AbstractAction("None") {
            public void actionPerformed(ActionEvent e) {
                annotationtype = ImageMarker.NONE;
            }
        });
        group.add(radio);
        annotation.add(radio);

        
        // Style of annotation
//        annotation.addSeparator();
//        group = new ButtonGroup();
//        radio = new JRadioButtonMenuItem(new AbstractAction("Solid") {
//            public void actionPerformed(ActionEvent e) {
//                annotationstyle = ImageMarker.SOLID;
//            }
//        });
//        radio.setSelected(true);
//        group.add(radio);
//        annotation.add(radio);
//
//        radio = new JRadioButtonMenuItem(new AbstractAction("Dashed") {
//            public void actionPerformed(ActionEvent e) {
//                annotationstyle = ImageMarker.DASHED;
//            }
//        });
//        group.add(radio);
        annotation.add(radio);

        // location of annotation
        annotation.addSeparator();
        group = new ButtonGroup();
        radio = new JRadioButtonMenuItem(new AbstractAction("Selection") {
            public void actionPerformed(ActionEvent e) {
                annotationselection = true;
            }
        });
        radio.setSelected(true);
        group.add(radio);
        annotation.add(radio);

        radio = new JRadioButtonMenuItem(new AbstractAction("Clicked") {
            public void actionPerformed(ActionEvent e) {
                annotationselection = false;
            }
        });
        group.add(radio);
        annotation.add(radio);

        annotation.addSeparator();
        
        menu = new JMenuItem(new AbstractAction("Apply to image") {
            public void actionPerformed(ActionEvent e) {
            	try {
            		ImageObject orgIm = imagepanel.getImageObject();
            		
            		BufferedImage im = orgIm.makeBufferedImage();
            		Graphics2D g2d = (Graphics2D)im.getGraphics();
            		
            		for (Iterator iter = annotations.iterator(); iter.hasNext();) {
            			((ImageMarker) iter.next()).paint(g2d, null);
            		}
	            	
	            	ImageObject imobj = ImageObject.getImageObject(im);
	            	
	            	
	            	HashMap<String, Object> hm = imagepanel.getImageObject().getProperties();
	            	hm.remove("_MIP");
	            	imobj.setProperties(hm);
	            	imagepanel.setImageObject(imobj);
				
            	} catch (ImageException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            	
            }
        });
        annotation.add(menu);

        
        
        // location of annotation
//        annotation.addSeparator();
//        group = new ButtonGroup();
//        radio = new JRadioButtonMenuItem(new AbstractAction("Label") {
//            public void actionPerformed(ActionEvent e) {
//                labelselection = true;
//                labelfd.setVisible(true);
//            }
//        });
//        group.add(radio);
//        annotation.add(radio);
//
//        radio = new JRadioButtonMenuItem(new AbstractAction("No Label") {
//            public void actionPerformed(ActionEvent e) {
//                labelselection = false;
//                labelfd.setVisible(false);
//            }
//        });
//        radio.setSelected(true);
//        group.add(radio);
//        annotation.add(radio);
        
        return new JMenuItem[]{annotation};
    }

    public JMenuItem[] getMainMenuItems() {
        return null;
    }

    public void imageUpdated(ImageUpdateEvent event) {
    }

    public URL getHelp(String topic) {
        // TODO Auto-generated method stub
        return null;
    }
}
