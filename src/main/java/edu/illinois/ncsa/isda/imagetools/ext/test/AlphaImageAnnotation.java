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
package edu.illinois.ncsa.isda.imagetools.ext.test;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.display.ImageAnnotation;
import edu.illinois.ncsa.isda.imagetools.core.display.ImagePanel;


/**
 * Created by IntelliJ IDEA. User: kooper Date: Feb 2, 2005 Time: 3:31:25 PM To change this template use File | Settings |
 * File Templates.
 */
public class AlphaImageAnnotation implements ImageAnnotation {
	private double			alpha	= 0;
	private ImageObject		imgobj;
	private BufferedImage	image;
	private int				y;
	private int				x;
	private double			r;

	public void setImageObject(ImageObject imgobj) {
		this.imgobj = imgobj;
		if (imgobj == null) {
			this.image = null;
		} else {
			image = imgobj.toBufferedImage(image, false, null, false, 0, 1, 2, false, 0, null, alpha, 1.0);
		}
		System.gc();
	}

	public void setAlpha(double alpha) {
		this.alpha = alpha;
		if (imgobj == null) {
			image = null;
		} else {
			image = imgobj.toBufferedImage(image, false, null, false, 0, 1, 2, false, 0, null, alpha, 1.0);
		}
		System.gc();
	}

	public double getAlpha() {
		return alpha;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getX() {
		return x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getY() {
		return y;
	}

	public void setRotation(double r) {
		this.r = r;
	}

	public double getRotation() {
		return r;
	}

	public void paint(Graphics2D g, ImagePanel imagepanel) {
		if (image != null) {
			g.translate(x + image.getWidth() / 2.0, y + image.getHeight() / 2.0);
			g.rotate(r);
			g.translate(-image.getWidth() / 2.0, -image.getHeight() / 2.0);
			g.drawImage(image, 0, 0, null);
		}
	}
}
