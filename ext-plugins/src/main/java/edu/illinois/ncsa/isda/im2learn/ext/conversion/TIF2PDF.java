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
package edu.illinois.ncsa.isda.im2learn.ext.conversion;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.ElementIterator;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.io.FileChooser;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageLoader;
import edu.illinois.ncsa.isda.im2learn.ext.pdf.PDFAnnotationDialog;

/**
 * 
 * 
 */
public class TIF2PDF {
    
    public TIF2PDF() {
    }

	
    public static void convert(String inFilename, String outFilename, int dpi, float inch) throws Exception {
    
    	int width = 600;
    	
    	Document document = new Document();	
    	PdfWriter.getInstance(document, new FileOutputStream(outFilename));
    	
    	document.open();
    	ImageObject inputIm = ImageLoader.readImage(inFilename);
    	System.out.println("Converting");
    	
    	java.awt.Image jim = inputIm.makeBufferedImage();
			
    	Image pdfIm = Image.getInstance(jim, null);
    	pdfIm.setAlignment(Image.ALIGN_CENTER);
    	if(dpi > 0) {
    		float fac = 100F * (72F/(float)dpi) * (inch/8.26F);
    		System.out.println("Scaling factor = "+fac);
			pdfIm.scalePercent(fac); // original size
			
//				pdfIm.scalePercent(100*(float)width/(float)inputIm.getNumCols());
    	}
    	document.add(pdfIm);
    	
    	System.out.println("Writing");
    	
    	document.close();
    	System.out.println("DONE");
    }

    	
	
	
    static public void main(String[] args) {
    
    	FileChooser file = new FileChooser();
    	String inFilename = null;
    	String outFilename = null;
    	
    	try {
			inFilename = file.showOpenDialog();
			outFilename = file.showSaveDialog();
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(inFilename == null ||
		   outFilename == null) {
			return;
		}
    	
		
		
    	try {
    		TIF2PDF.convert(inFilename, outFilename, 600, 5F);
    	} catch (Exception e) {
    		e.printStackTrace();
		}
    }
 }


