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
package edu.illinois.ncsa.isda.imagetools.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.display.ImageComponent;
import edu.illinois.ncsa.isda.imagetools.ext.hyperspectral.ConvertRGB;


public class CreateLogo {
	static public void main(String[] args) throws Exception {
		// create the spectrum
		ImageObject ioSpectrum = ConvertRGB.getBruton(380, 720, 200).getSpectrum();
		ioSpectrum = ioSpectrum.scale(48.0 / ioSpectrum.getNumCols(), 48.0 / ioSpectrum.getNumRows());		
		BufferedImage biSpectrum = ioSpectrum.makeBufferedImage();
		
		// create the logo
		BufferedImage logo = new BufferedImage(48, 48, BufferedImage.TYPE_INT_RGB);
		Graphics g = logo.getGraphics();
		g.setColor(Color.white);
		g.fillRect(0, 0, 48, 48);
		
		// draw spectrum in logo
		g.drawImage(biSpectrum, 0, 20, 48, 8, null);
		
		// draw the name
		g.setColor(Color.black);
		g.setFont(new Font("Ariel", Font.PLAIN, 10));
		g.drawString("im2learn", 2, 12);

		// draw the NCSA
		g.setColor(Color.black);
		g.setFont(new Font("Ariel", Font.PLAIN, 10));
		g.drawString("NCSA", 12, 42);

		// show the logo
		JFrame frm = new JFrame();
		frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//frm.getContentPane().add(new JLabel(new ImageIcon(logo)));
		frm.getContentPane().add(new ImageComponent(ioSpectrum));
		frm.pack();
		frm.setVisible(true);
	}
}
