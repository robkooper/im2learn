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
package edu.illinois.ncsa.isda.im2learn.core.io.pdf;

import org.jpedal.PdfDecoder;
import org.jpedal.exception.PdfException;
import org.jpedal.fonts.PdfFont;
import org.jpedal.grouping.PdfGroupingAlgorithms;
import org.jpedal.io.ObjectStore;
import org.jpedal.objects.PdfData;
import org.jpedal.objects.PdfImageData;
import org.jpedal.objects.PdfPageData;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageException;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.datatype.PDFAnnotation;
import edu.illinois.ncsa.isda.im2learn.core.datatype.SubArea;
import edu.illinois.ncsa.isda.im2learn.core.display.Im2LearnMenu;
import edu.illinois.ncsa.isda.im2learn.core.display.ImagePanel;
import edu.illinois.ncsa.isda.im2learn.core.display.ImageUpdateEvent;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageLoader;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageReader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA. User: kooper Date: Apr 25, 2005 Time: 2:56:21 PM To
 * change this template use File | Settings | File Templates.
 */
public class PDFLoaderJPedal  implements ImageReader, Im2LearnMenu {
    private static boolean useAnnotations = false;

    static public void setUseAnnotations(boolean use) {
        PDFLoaderJPedal.useAnnotations = use;
    }

    static public boolean isUseAnnotations() {
        return PDFLoaderJPedal.useAnnotations;
    }
    
    /**
     * @deprecated use getImageCount instead.
     */
    public int getPageCount(String filename) throws ImageException {
    	try {
    		return getImageCount(filename);
    	} catch (IOException e) {
    		throw(new ImageException(e));
    	}
    }

    // ------------------------------------------------------------------------
    // ImageReader implementation
    // ------------------------------------------------------------------------
    public boolean canRead(String filename, byte[] hdr) {
        return (new String(hdr, 0, 5).equals("%PDF-"));
    }

    public int getImageCount(String filename) throws IOException, ImageException {
    	try {
	    	PdfDecoder decoder = new PdfDecoder();
            decoder.openPdfFile(filename);
            int count = decoder.getPageCount();
	        decoder.closePdfFile();
	        return count;
        } catch (PdfException exc) {
    		throw(new ImageException(exc));
    	}
	}

	public ImageObject readImage(String filename, int index, SubArea subarea, int sampling) throws IOException, ImageException {
        float scale = 2;
        int dpi = 72;

        if (useAnnotations) {
        	return readImageAnnotation(filename, index+1, dpi, scale, false);
        } else {
        	return readImage(filename, index+1, scale, false);
        }
	}

    public ImageObject readImage(String filename, int pageno, int dpi, float scale) throws IOException, ImageException {
        if (useAnnotations) {
        	return readImageAnnotation(filename, pageno, dpi, scale, false);
        } else {
        	return readImage(filename, pageno, scale, false);
        }
    }

    public ImageObject readImage(String filename, int pageno, int dpi, float scale, boolean intimage) throws IOException, ImageException {
        if (useAnnotations) {
        	return readImageAnnotation(filename, pageno, dpi, scale, intimage);
        } else {
        	return readImage(filename, pageno, scale, intimage);
        }
    }

    public ImageObject readImage(String filename, SubArea subarea, int sampling) throws IOException, ImageException {
    	return readImage(filename, 0, subarea, sampling);
    }
    
    public ImageObject readImage(String filename, SubArea subarea, int sampling, boolean intimage) throws IOException, ImageException {
        int pageno = 1;
        float scale = 2;
        int dpi = 300;

        if (useAnnotations) {
        	return readImageAnnotation(filename, pageno, dpi, scale, intimage);
        } else {
        	return readImage(filename, pageno, scale, intimage);
        }
    }
    
    private ImageObject readImage(String filename, int pageno, float scale, boolean intimage) throws ImageException {
    	try {
	    	PdfDecoder decoder = new PdfDecoder();
            decoder.openPdfFile(filename);
	       	decoder.setPageParameters(scale, pageno);
	        ImageObject imgobj = ImageObject.getImageObject(decoder.getPageAsImage(pageno), intimage);
	        decoder.closePdfFile();
	        return imgobj;
    	} catch (PdfException exc) {
    		throw(new ImageException(exc));
    	}
    }
    
    private ImageObject readImageAnnotation(String filename, int pageno, int dpi, float scale, boolean intimage) throws IOException {
        try {
            //PdfDecoder.useTextExtraction();
            PdfFont.setStandardFontMappings();

            PdfDecoder decoder = new PdfDecoder(true);
           // decoder.enableHighQualityMode();
            decoder.openPdfFile(filename);
            if (useAnnotations) {
            	decoder.setExtractionMode(PdfDecoder.CLIPPEDIMAGES + PdfDecoder.TEXT + PdfDecoder.TEXTCOLOR, dpi, scale);
            }
            decoder.init(true);
            decoder.decodePage(pageno);

            // get page information
            PdfPageData currentPageData = decoder.getPdfPageData();
            Rectangle media = new Rectangle(currentPageData.getMediaBoxX(pageno),
                                            currentPageData.getMediaBoxY(pageno),
                                            currentPageData.getMediaBoxWidth(pageno),
                                            currentPageData.getMediaBoxHeight(pageno));
            Rectangle crop = new Rectangle(currentPageData.getCropBoxX(pageno),
                                           currentPageData.getCropBoxY(pageno),
                                           currentPageData.getCropBoxWidth(pageno),
                                           currentPageData.getCropBoxHeight(pageno));

            // image is with cropbox, rest with mediabox. sigh
            int dx = media.x - crop.x;
            int dy = media.y - crop.y;

            // get the page as an image
            BufferedImage image = decoder.getPageAsImage(pageno);
            ImageObject imgobj = ImageObject.getImageObject(image, intimage);

            // add the fonts used in the file as a property
            imgobj.setProperty("FontsUsed", decoder.getFontsInFile());

            // get annotations
            Vector annotations = new Vector();

            PdfData text = decoder.getPdfData();

            // extract the text
            new PdfGroupingAlgorithms().cleanupText(text);
            int items = text.getTextElementCount();
            for (int i = 0; i < items; i++) {
                Map txt = text.getTextElementAt(i);

                // extract the text
                String str = txt.get("content").toString();

                // extract the bounding-box of the text
                double x = Double.parseDouble(txt.get("x1").toString());
                double y = Double.parseDouble(txt.get("y1").toString());
                double w = Math.abs(Double.parseDouble(txt.get("x2").toString()) - x);
                double h = Math.abs(Double.parseDouble(txt.get("y2").toString()) - y);

                // adjust for the difference between crop box and media box
                x = dx + x - media.x;
                y = dy + y - media.y;

                // ???
                y = crop.height - y;

                // round off
                //x = Math.ceil(x);
                //y = Math.floor(y);
                //w = Math.ceil(w);
                //h = Math.ceil(h);

                // add to the vector
                annotations.add(new PDFAnnotation(scale * x, scale * y, scale * w, scale * h, str));
            }

            // extract the text
            //            PdfData text = decoder.getPdfData();
            //            PdfGroupingAlgorithms grouping = new PdfGroupingAlgorithms(text);
            //            Vector words = grouping.extractTextAsWordlist((int)media.getMinX(), (int)media.getMaxY(), (int)media.getMaxX(), (int)media.getMinY(),
            //                                                          pageno, false, false, "");
            //
            //            // draw a green box around each word.
            //            if (words.size() > 0) {
            //                for(Iterator iter=words.iterator(); iter.hasNext(); ) {
            //                    // extract the text
            //                    String str =(String)iter.next();
            //
            //                    // extract the bounding-box of the text
            //                    double x = Double.parseDouble((String)iter.next());
            //                    double y = Double.parseDouble((String)iter.next());
            //                    double w = Math.abs(Double.parseDouble((String)iter.next()) - x);
            //                    double h = Math.abs(Double.parseDouble((String)iter.next()) - y);
            //
            //                    // adjust for the difference between crop box and media box
            //                    x = dx + x;
            //                    y = dy + y;
            //
            //                    // ???
            //                    y = crop.height - y;
            //
            //                    // round off
            //                    x = Math.ceil(x);
            //                    y = Math.floor(y);
            //                    w = Math.ceil(w);
            //                    h = Math.ceil(h);
            //
            //                    // add to the vector
            //                    PDFAnnotation anno = new PDFAnnotation(scale*x, scale*y, scale*w, scale*h, str);
            //                    annotations.add(anno);
            //                }
            //            }

            // extract the images
            PdfImageData images = decoder.getPdfImageData();

            if (images.getImageCount() > 0) {
                double sc_x = dpi / (72 * scale);
                // draw blue bounding boxes around all the images
                for (int i = 0; i < images.getImageCount(); i++) {
                    // get bounding box of the image
                    double x = images.getImageXCoord(i);
                    double y = images.getImageYCoord(i);
                    double w = images.getImageWidth(i);
                    double h = images.getImageHeight(i);

                    // adjust for the difference between crop box and media box
                    x = dx + x;
                    y = dy + y;

                    // correct for bounding boxes given from top of
                    y = media.height - y - h;

                    // save for later
                    BufferedImage img = decoder.getObjectStore().loadStoredImage(images.getImageName(i));
                    if (img == null) {
                    	annotations.add(new PDFAnnotation(scale * x, scale * y, scale * w, scale * h, "NULL IMAGE"));
                    } else {
                    	annotations.add(new PDFAnnotation(scale * x, scale * y, scale * w, scale * h, ImageObject.getImageObject(img)));
                    }
                }
            }

            imgobj.setProperty(PDFAnnotation.KEY, annotations);

            // done close the file
            decoder.closePdfFile();

            // return the image
            return imgobj;
        } catch (Exception exc) {
        	exc.printStackTrace();
            throw(new IOException(exc.toString()));
        }    	
    }

    public ImageObject readImageHeader(String filename) throws IOException, ImageException {
		return readImageHeader(filename, 1);
    }

	public ImageObject readImageHeader(String filename, int index) throws IOException, ImageException {
        throw new ImageException("Not implemented.");
	}

    public String[] readExt() {
        return new String[]{"pdf"};
    }

    public String getDescription() {
        return "PDF Files - JPedal";
    }

	// ------------------------------------------------------------------------
    // Im2LearnMenu implementation
    // ------------------------------------------------------------------------
    public void setImagePanel(ImagePanel imagepanel) {
    }

    public JMenuItem[] getPanelMenuItems() {
        return null;
    }

    public JMenuItem[] getMainMenuItems() {
    	JMenu file = new JMenu("File");
    	JMenu prefs = new JMenu("Preferences");
    	JMenu me = new JMenu("JPedal");
    	JCheckBoxMenuItem chk = new JCheckBoxMenuItem(new AbstractAction("PDF Annotations") {
			public void actionPerformed(ActionEvent evt) {
				JCheckBoxMenuItem chk = (JCheckBoxMenuItem)evt.getSource();
				setUseAnnotations(chk.isSelected());
			}
    	});
    	chk.setSelected(true);
    	setUseAnnotations(chk.isSelected());
    	file.add(prefs);
    	prefs.add(me);
    	me.add(chk);
    	return new JMenuItem[]{file};
    }

    public void imageUpdated(ImageUpdateEvent event) {
    }

    public URL getHelp(String topic) {
        return null;
    }
}
