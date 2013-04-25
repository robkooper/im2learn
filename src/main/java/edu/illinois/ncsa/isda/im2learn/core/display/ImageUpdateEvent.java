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
package edu.illinois.ncsa.isda.im2learn.core.display;

import java.util.EventObject;

public class ImageUpdateEvent extends EventObject {
    public static final int UNKNOWN = 0;

    // fired by imagecomponent
    public static final int NEW_IMAGE = 1;
    public static final int CHANGE_CROP = 2;
    public static final int CHANGE_AUTOZOOM = 3;
    public static final int CHANGE_ZOOMFACTOR = 4;
    public static final int CHANGE_GAMMA = 5;
    public static final int CHANGE_REDBAND = 6;
    public static final int CHANGE_GREENBAND = 7;
    public static final int CHANGE_BLUEBAND = 8;
    public static final int CHANGE_RGBBAND = 9;
    public static final int CHANGE_GRAYBAND = 10;
    public static final int CHANGE_GRAYSCALE = 11;
    public static final int CHANGE_PSEUDOCOLOR = 12;
    public static final int CHANGE_USETOTALS = 13;
    public static final int CHANGE_USERSCALE = 14;
    public static final int CHANGE_USERSCALEVALUE = 15;
    public static final int CHANGE_VISIBLEREGION = 16;
	public static final int CHANGE_PAINTSCALE = 17;

    // fired by imagepanel
    public static final int ADD_ANNOTATION = 20;
    public static final int REMOVE_ANNOTATION = 21;
    public static final int ALLOW_SELECTION = 22;
    public static final int CHANGE_SELECTION = 23; 
    
    public static final int MOUSE_CLICKED = 24;

    // TODO remove this one, only temp
    //public static final int TODO = Integer.MAX_VALUE;

    private int id = UNKNOWN;
    private Object obj = null;

    public ImageUpdateEvent(Object source) {
        this(source, UNKNOWN, null);
    }

    public ImageUpdateEvent(Object source, int id, Object obj) {
        super(source);
        this.id = id;
        this.obj = obj;
    }

    public int getId() {
        return id;
    }

    public Object getObject() {
        return obj;
    }
}
