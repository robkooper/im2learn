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
package edu.illinois.ncsa.isda.imagetools.ext.pdf;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;
import java.util.Vector;

import edu.illinois.ncsa.isda.imagetools.core.io.pdf.PDFAnnotation;


public class PDFAnnotationGroup implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final String KEY = "PDFAnnotationGroup";

    private Vector<PDFAnnotation> annotations;

    private double x, y, w, h;
    private String uuid;
    
    private HashMap<String, Object> properties;

    public PDFAnnotationGroup() {
        uuid = UUID.randomUUID().toString();
        annotations = new Vector<PDFAnnotation>();
        properties = new HashMap<String, Object>();
    }

    public String getUUID() {
        return uuid;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }
    
    public Object getProperty(String key) {
        return properties.get(key);
    }
    
    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }

    public String[] getPropertyKeys() {
        return properties.keySet().toArray(new String[0]);
    }

    public void setProperty(String key, Object val) {
        properties.put(key, val);
    }

    public boolean contains(PDFAnnotation anno) {
        return annotations.contains(anno);
    }

    public void recalculate() {
        x = annotations.get(0).getX();
        y = annotations.get(0).getY();
        double mx = annotations.get(0).getMaxX();
        double my = annotations.get(0).getMaxY();

        for (PDFAnnotation anno : annotations) {
            if (anno.getX() < x) {
                x = anno.getX();
            }
            if (anno.getY() < y) {
                y = anno.getY();
            }
            if (anno.getMaxX() > mx) {
                mx = anno.getMaxX();
            }
            if (anno.getMaxY() > my) {
                my = anno.getMaxY();
            }
        }

        w = mx - x;
        h = my - y;
    }

    public void remove(PDFAnnotation anno) {
        if (!annotations.contains(anno)) {
            return;
        }
        annotations.remove(anno);
        if (annotations.isEmpty()) {
            return;
        }

        recalculate();
    }

    public void add(PDFAnnotation anno) {
        if (annotations.isEmpty()) {
            annotations.add(anno);

            x = anno.getX();
            y = anno.getY();
            w = anno.getWidth();
            h = anno.getHeight();
        } else if (!annotations.contains(anno)) {
            annotations.add(anno);

            double mx = getMaxX();
            double my = getMaxY();

            if (anno.getX() < x) {
                x = anno.getX();
            }
            if (anno.getY() < y) {
                y = anno.getY();
            }
            if (anno.getMaxX() > mx) {
                mx = anno.getMaxX();
            }
            if (anno.getMaxY() > my) {
                my = anno.getMaxY();
            }
            w = mx - x;
            h = my - y;
        }
    }

    public Vector<PDFAnnotation> get() {
        return annotations;
    }

    public double getX() {
        return x;
    }

    public double getMaxX() {
        return x + w;
    }

    public double getY() {
        return y;
    }

    public double getMaxY() {
        return y + h;
    }

    public double getWidth() {
        return w;
    }

    public double getHeight() {
        return h;
    }

    public Rectangle2D getBoundingBox() {
        return new Rectangle2D.Double(x, y, w, h);
    }

    public void drawBoundingBox(Graphics g) {
        drawBoundingBox(g, null);
    }

    public void drawBoundingBox(Graphics g, Color color) {
        if (!annotations.isEmpty()) {
            g.setColor((color == null) ? Color.black : color);
            g.drawRect((int) x, (int) y, (int) w, (int) h);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PDFAnnotationGroup)) {
            return false;
        }
        PDFAnnotationGroup feature = (PDFAnnotationGroup) obj;
        if (!feature.getBoundingBox().equals(getBoundingBox())) {
            return false;
        }

        // TODO should compare annotation as well
        return true;
    }

    public String toString() {
        if (annotations.isEmpty()) {
            return "no annotations associated with this feature.";
        }

        String result = "UUID = " + uuid + "\n";
        result += "[" + x + ", " + y + ", " + w + ", " + h + "]\n";

        for (PDFAnnotation anno : annotations) {
            result += anno.toString() + "\n";
            // if (anno.isImage()) {
            // result += "\tIMAGE : "+ anno.getObject().toString();
            // } else {
            // result += "\tTEXT : " + anno.getObject().toString();
            // }
            // result += "\n";
        }

        return result;
    }
}
