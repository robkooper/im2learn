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
package edu.illinois.ncsa.isda.im2learn.ext.pdf;

import java.util.Iterator;
import java.util.Vector;

import edu.illinois.ncsa.isda.im2learn.core.datatype.PDFAnnotation;

/**
 * Created by IntelliJ IDEA. User: kooper Date: May 2, 2005 Time: 9:57:38 AM To
 * change this template use File | Settings | File Templates.
 */
public class PDFAnnotationTextCleanup {
    private Vector<PDFAnnotation> annotations;
    private double                dx;
    private double                dy;

    public PDFAnnotationTextCleanup() {
        annotations = new Vector<PDFAnnotation>();

        dx = 0;
        dy = 0;
    }

    public PDFAnnotationTextCleanup(Vector<PDFAnnotation> annotations) {
        this.annotations = annotations;

        dx = 0;
        dy = 0;
    }

    public void setTolerance(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public void reset(Vector<PDFAnnotation> annotations) {
        this.annotations = annotations;
    }

    // ------------------------------------------------------------------------
    // Clean PDF Text
    // ------------------------------------------------------------------------
    public void removeDuplicates() {
        // find all double text
        Vector<PDFAnnotation> newtxt = new Vector<PDFAnnotation>();
        for (PDFAnnotation objold : annotations) {
            if (objold.isText() && !objold.isDuplicate()) {
                for (PDFAnnotation objnew : newtxt) {
                    if (objnew.isText() && objnew.getObject().equals(objold.getObject())) {
                        if (checkSameLocation(objold, objnew)) {
                            objold.setDuplicate(true);
                            break;
                        }
                    }
                }
            }
            newtxt.add(objold);
        }
    }

    public void mergeLine() {
        // find all text that is potentially a continuation
        Vector<PDFAnnotation> newtxt = new Vector<PDFAnnotation>();
        for (Iterator iterold = annotations.iterator(); iterold.hasNext();) {
            PDFAnnotation objold = (PDFAnnotation) iterold.next();
            if (objold.isText() && !objold.isDuplicate()) {
                boolean ignore = false;
                for (Iterator iternew = newtxt.iterator(); iternew.hasNext() && !ignore;) {
                    PDFAnnotation objnew = (PDFAnnotation) iternew.next();
                    if (objnew.isText() && checkSameLine(objold, objnew)) {
                        objnew.add(objold, "");
                        ignore = true;
                    }
                }
                if (!ignore) {
                    newtxt.add(objold);
                } else {
                    iterold.remove();
                }
            } else {
                newtxt.add(objold);
            }
        }
    }

    public void mergePara() {
        // find all text that is potentially a continuation
        Vector<PDFAnnotation> newtxt = new Vector<PDFAnnotation>();
        for (Iterator iterold = annotations.iterator(); iterold.hasNext();) {
            PDFAnnotation objold = (PDFAnnotation) iterold.next();
            if (objold.isText() && !objold.isDuplicate() && !((String) objold.getObject()).trim().equals("")) {
                boolean ignore = false;
                for (Iterator iternew = newtxt.iterator(); iternew.hasNext() && !ignore;) {
                    PDFAnnotation objnew = (PDFAnnotation) iternew.next();
                    if (objnew.isText() && checkSamePara(objold, objnew) && !((String) objnew.getObject()).trim().equals("")) {
                        objnew.add(objold, "\n");
                        ignore = true;
                    }
                }
                if (!ignore) {
                    newtxt.add(objold);
                } else {
                    iterold.remove();
                }
            } else {
                newtxt.add(objold);
            }
        }
        annotations = newtxt;
    }

    /**
     * remove all invalid annotations based on minimum dimension criteria
     */
    public void removeInvalidTextMinDimension(double minWidth, double minHeight) {
        // find all invalid images
        for (Iterator iterold = annotations.iterator(); iterold.hasNext();) {
            PDFAnnotation objold = (PDFAnnotation) iterold.next();
            if (objold.isText()) {
                if (objold.getWidth() < minWidth || objold.getHeight() < minHeight) {
                    // /test
                    // System.out.println("TEST: invalid obj "+objold);
                    objold.setInvalid(true);
                    objold.setClassification(PDFAnnotation.DIM_INVALID);
                }
            }
        }
    }

    /**
     * remove all invalid annotations based on minimum area criterion
     */
    public void removeInvalidTextMinArea(double minArea) {
        // find all invalid images
        for (Iterator iterold = annotations.iterator(); iterold.hasNext();) {
            PDFAnnotation objold = (PDFAnnotation) iterold.next();
            if (objold.isText()) {
                if (objold.getWidth() * objold.getHeight() < minArea) {
                    // /test
                    // System.out.println("TEST: invalid obj "+objold);
                    objold.setInvalid(true);
                    objold.setClassification(PDFAnnotation.AREA_INVALID);
                }
            }
        }
    }

    private boolean checkSameLocation(PDFAnnotation anno1, PDFAnnotation anno2) {
        return ((Math.abs(anno1.getX() - anno2.getX()) < dx) && (Math.abs(anno1.getY() - anno2.getY()) < dy));
    }

    private boolean checkSameLine(PDFAnnotation anno1, PDFAnnotation anno2) {
        // should start at the same y loc
        if (Math.abs(anno1.getY() - anno2.getY()) > dy) {
            return false;
        }

        // text should be same height
        if (anno1.isText() && anno2.isText()) {
            String t1 = (String) anno1.getObject();
            String t2 = (String) anno2.getObject();
            double h1 = anno1.getHeight() / t1.split("\n").length;
            double h2 = anno2.getHeight() / t2.split("\n").length;
            if (Math.abs(h1 - h2) > dy) {
                return false;
            }
        }

        // x location should be inside of bounding box of other text
        if ((anno2.getX() > anno1.getX() - dx) && (anno2.getX() < anno1.getMaxX() + dx)) {
            return true;
        }
        if ((anno1.getX() > anno2.getX() - dx) && (anno1.getX() < anno2.getMaxX() + dx)) {
            return true;
        }

        return false;
    }

    private boolean checkSamePara(PDFAnnotation anno1, PDFAnnotation anno2) {
        // text should be same height
        if (anno1.isText() && anno2.isText()) {
            String t1 = (String) anno1.getObject();
            String t2 = (String) anno2.getObject();
            double h1 = anno1.getHeight() / t1.split("\n").length;
            double h2 = anno2.getHeight() / t2.split("\n").length;
            if (Math.abs(h1 - h2) > dy) {
                return false;
            }
        }

        // should start at the same x loc (only if left justified)
        // if (Math.abs(anno1.getX() - anno2.getX()) > dx) {
        // return false;
        // }
        // make sure X is inside bounding box
        boolean xok = false;
        if ((anno2.getX() > anno1.getX() - dx) && (anno2.getX() < anno1.getMaxX() + dx)) {
            xok = true;
        }
        if ((anno1.getX() > anno2.getX() - dx) && (anno1.getX() < anno2.getMaxX() + dx)) {
            xok = true;
        }
        if (!xok) {
            return false;
        }

        // y location should be inside of bounding box of other text
        if ((anno2.getY() > anno1.getY() - dy) && (anno2.getY() < anno1.getMaxY() + dy)) {
            return true;
        }
        if ((anno1.getY() > anno2.getY() - dy) && (anno1.getY() < anno2.getMaxY() + dy)) {
            return true;
        }

        return false;
    }
}
