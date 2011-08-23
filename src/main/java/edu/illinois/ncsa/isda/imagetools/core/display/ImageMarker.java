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
package edu.illinois.ncsa.isda.imagetools.core.display;


import java.awt.*;

import edu.illinois.ncsa.isda.imagetools.core.datatype.SubArea;

/**
 * Created by IntelliJ IDEA. User: kooper Date: Mar 11, 2004 Time: 4:45:55 PM To
 * change this template use File | Settings | File Templates.
 */
public class ImageMarker extends SubArea implements ImageAnnotation {
    static final public int CROSS = 0;
    static final public int CIRCLE = 1;
    static final public int RECTANGLE = 2;
    static final public int LINE = 3; 
    static final public int LINEDOWN = LINE;
    static final public int LINEUP = 4;
    static final public int NONE = 10;

//    static final public BasicStroke DASHED = new BasicStroke(1, BasicStroke.CAP_SQUARE,
//                                                             BasicStroke.JOIN_MITER,
//                                                             10, new float[]{5}, 0);
//    static final public BasicStroke SOLID = new BasicStroke(1);

    private boolean filled = false;
    private boolean visible = false;
    private int type = RECTANGLE;
  //  private Stroke stroke = SOLID;
    private Color color = Color.BLACK;

    private String label = null;
    private int lblx = 0;
    private int lbly = 0;
    private Font fntLabel = null;

    public ImageMarker() {
    }

    public ImageMarker(int x, int y, int w, int h, int type) {
        super(x, y, w, h);
        this.type = type;
    }

    public ImageMarker(int x, int y, int r, int type) {
        super(x, y, r, r);
        this.type = type;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

//    public Stroke getStroke() {
//        return stroke;
//    }
//
//    public void setStroke(Stroke stroke) {
//        this.stroke = stroke;
//    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        setLabel(label, 0, 0, null);
    }

    public void setLabel(String label, int dx, int dy, Font font) {
        this.label = label;
        this.lblx = dx;
        this.lbly = dy;
        this.fntLabel = font;
    }
    
    public boolean isFilled() {
    	return filled;
    }
    
    public void setFilled(boolean f) {
    	filled = f;
    }

    public boolean equals(Object obj) {
        return obj == this;
        //        if (obj instanceof ImageMarker) {
        //            ImageMarker m = (ImageMarker)obj;
        //            return ((x == m.x) &&
        //                (y == m.y) &&
        //                (width == m.width) &&
        //                (height == m.height) &&
        //                    type == m.getType());
        //        }
        //        return false;
    }

    public void paint(Graphics2D g, ImagePanel imagepanel) {
        if (visible) {
            Color oldcol = g.getColor();
            Stroke oldstroke = g.getStroke();
            g.setColor(color);
          //  g.setStroke(stroke);
            g.translate(0.5, 0.5);

            switch (type) {
                case CROSS:
                    g.drawLine(x - (width / 2), y, x + (width / 2), y);
                    g.drawLine(x, y - (height / 2), x, y + (height / 2));
                    break;

                case LINEDOWN:
                    g.drawLine(x - (width / 2), y - (height / 2), x + (width / 2), y + (height / 2));
                    break;
                    
                case LINEUP:
                    g.drawLine(x - (width / 2), y + (height / 2), x + (width / 2), y - (height / 2));
                    break;
                    
                case CIRCLE:
                	if (filled) {
                		g.fillOval(x - (width / 2), y - (height / 2), width, height);
                	} else {
                		g.drawOval(x - (width / 2), y - (height / 2), width, height);
                	}
                    break;
                    
                case NONE:
                	break;

                case RECTANGLE:
                
                default:
            		if (filled) {
                		g.fillRect(x, y, width, height);
                	} else {
                		g.drawRect(x, y, width, height);
                	}
            }
            
            if (label != null) {
                Font oldfont = g.getFont();
                if (fntLabel != null) {
                    g.setFont(fntLabel);
                }
                g.drawString(label, x + lblx, y - lbly);
                g.setFont(oldfont);

            }

            g.translate(-0.5, -0.5);
            g.setColor(oldcol);
            g.setStroke(oldstroke);
        }
    }
}
