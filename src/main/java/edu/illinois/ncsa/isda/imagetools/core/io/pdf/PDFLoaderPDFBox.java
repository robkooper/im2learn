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
package edu.illinois.ncsa.isda.imagetools.core.io.pdf;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;


import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdfviewer.PageDrawer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.PDFOperator;
import org.apache.pdfbox.util.TextPosition;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageException;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.datatype.PDFAnnotation;
import edu.illinois.ncsa.isda.imagetools.core.datatype.SubArea;
import edu.illinois.ncsa.isda.imagetools.core.display.Im2LearnMenu;
import edu.illinois.ncsa.isda.imagetools.core.display.ImagePanel;
import edu.illinois.ncsa.isda.imagetools.core.display.ImageUpdateEvent;
import edu.illinois.ncsa.isda.imagetools.core.io.ImageReader;

public class PDFLoaderPDFBox implements ImageReader, Im2LearnMenu {
    private static boolean useAnnotations = false;

    static public void setUseAnnotations(boolean use) {
        PDFLoaderPDFBox.useAnnotations = use;
    }

    static public boolean isUseAnnotations() {
        return PDFLoaderPDFBox.useAnnotations;
    }

    // copied from PDPage this way I can add my own PageDrawer and find location
    // of text and graphics.
    public ImageObject convertToImage(PDPage page) throws IOException {
        int scaling = 2;
        int rotation = page.findRotation();
        PDRectangle mBox = page.getMediaBox();
        if (mBox == null) {
            mBox = page.getTrimBox();
        }
        int width = (int) (mBox.getWidth());//*2);
        int height = (int) (mBox.getHeight());//*2);
        if (rotation == 90 || rotation == 270) {
            int tmp = width;
            width = height;
            height = tmp;
        }
        Dimension pageDimension = new Dimension(width, height);

        //note we are doing twice as many pixels because
        //the default size is not really good resolution,
        //so create an image that is twice the size
        //and let the client scale it down.
        BufferedImage img = new BufferedImage(width * scaling, height * scaling, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = (Graphics2D) img.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, width * scaling, height * scaling);
        graphics.scale(scaling, scaling);
        MyPageDrawer drawer = new MyPageDrawer();
        drawer.drawPage(graphics, page, pageDimension);

        try {
            ImageObject imgobj = ImageObject.getImageObject(img);
            imgobj.setProperty(PDFAnnotation.KEY, drawer.getAnnotations());
            return imgobj;
        } catch (ImageException exc) {
            throw new IOException(exc.toString());
        }
    }

    // ------------------------------------------------------------------------
    // ImageReader implementation
    // ------------------------------------------------------------------------
    public boolean canRead(String filename, byte[] hdr) {
        return (new String(hdr, 0, 5).equals("%PDF-"));
    }

    public ImageObject readImage(String filename, SubArea subarea, int sampling) throws IOException, ImageException {
        ImageObject imgobj = null;

        PDDocument doc = PDDocument.load(filename);
        List pages = doc.getDocumentCatalog().getAllPages();
        PDPage page = (PDPage) pages.get(0);
        if (useAnnotations) {
            imgobj = convertToImage(page);
        } else {
            imgobj = ImageObject.getImageObject(page.convertToImage());
        }

        if (sampling != 1) {
            imgobj = imgobj.scale(sampling);
        }

        doc.close();
        return imgobj;
    }

    public ImageObject readImageHeader(String filename) throws IOException, ImageException {
        throw new ImageException("Not implemented.");
    }

    public String[] readExt() {
        return new String[] { "pdf" };
    }

    public String getDescription() {
        return "PDF Files - PDF Toolbox";
    }

    // ------------------------------------------------------------------------
    // PageDrawer to get rectangle size
    // ------------------------------------------------------------------------
    class MyPageDrawer extends PageDrawer {
        private final Vector annotations = new Vector();

        public MyPageDrawer() throws IOException {
            super();
        }

        public Vector getAnnotations() {
            return annotations;
        }

        @Override
        protected void processOperator(PDFOperator operator, List arguments) throws IOException {
            super.processOperator(operator, arguments);

            String operation = operator.getOperation();

            if (operation.equals("Do")) {
                COSName objectName = (COSName) arguments.get(0);
                Map xobjects = getResources().getXObjects();
                PDXObject xobject = (PDXObject) xobjects.get(objectName.getName());
                if (xobject instanceof PDXObjectImage) {
                    PDXObjectImage image = (PDXObjectImage) xobject;
                    try {
                        BufferedImage awtImage = image.getRGBImage();
                        Matrix ctm = getGraphicsState().getCurrentTransformationMatrix();

                        int width = awtImage.getWidth();
                        int height = awtImage.getHeight();

                        double rotationInRadians = (getPage().findRotation() * Math.PI) / 180;

                        AffineTransform rotation = new AffineTransform();
                        rotation.setToRotation(rotationInRadians);
                        AffineTransform rotationInverse = rotation.createInverse();
                        Matrix rotationInverseMatrix = new Matrix();
                        rotationInverseMatrix.setFromAffineTransform(rotationInverse);
                        Matrix rotationMatrix = new Matrix();
                        rotationMatrix.setFromAffineTransform(rotation);

                        Matrix unrotatedCTM = ctm.multiply(rotationInverseMatrix);

                        Matrix scalingParams = unrotatedCTM.extractScaling();
                        Matrix scalingMatrix = Matrix.getScaleInstance(1f / width, 1f / height);
                        scalingParams = scalingParams.multiply(scalingMatrix);

                        Matrix translationParams = unrotatedCTM.extractTranslating();
                        Matrix translationMatrix = null;
                        int pageRotation = getPage().findRotation();
                        if (pageRotation == 0) {
                            translationParams.setValue(2, 1, -translationParams.getValue(2, 1));
                            translationMatrix = Matrix.getTranslatingInstance(0, (float) getPageSize().getHeight() - height * scalingParams.getYScale());
                        } else if (pageRotation == 90) {
                            translationMatrix = Matrix.getTranslatingInstance(0, (float) getPageSize().getHeight());
                        } else {
                            //TODO need to figure out other cases
                        }
                        translationParams = translationParams.multiply(translationMatrix);

                        AffineTransform at = new AffineTransform(scalingParams.getValue(0, 0), 0, 0, scalingParams.getValue(1, 1), translationParams.getValue(2, 0), translationParams.getValue(2, 1));

                        //at.setToTranslation( pageSize.getHeight()-ctm.getValue(2,0),ctm.getValue(2,1) );
                        //at.setToScale( ctm.getValue(0,0)/width, ctm.getValue(1,1)/height);
                        //at.setToRotation( (page.findRotation() * Math.PI)/180 );

                        //AffineTransform rotation = new AffineTransform();
                        //rotation.rotate( (90*Math.PI)/180);

                        /*

                        // The transformation should be done
                        // 1 - Translation
                        // 2 - Rotation
                        // 3 - Scale or Skew
                        AffineTransform at = new AffineTransform();

                        // Translation
                        at = new AffineTransform();
                        //at.setToTranslation((double)ctm.getValue(0,0),
                        //                    (double)ctm.getValue(0,1));

                        // Rotation
                        //AffineTransform toAdd = new AffineTransform();
                        toAdd.setToRotation(1.5705);
                        toAdd.setToRotation(ctm.getValue(2,0)*(Math.PI/180));
                        at.concatenate(toAdd);
                        */

                        // Scale / Skew?
                        //toAdd.setToScale(1, 1);
                        //at.concatenate(toAdd);
                        //graphics.drawImage( awtImage, at, null );
                        // code to get bounding box
                        at.preConcatenate(getGraphics().getTransform());

                        Rectangle2D rect = new Rectangle2D.Double(0, 0, awtImage.getWidth(), awtImage.getHeight());
                        rect = transform(at, rect);
                        annotations.add(new PDFAnnotation(rect, ImageObject.getImageObject(awtImage)));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        protected void processTextPosition(TextPosition text) {
			super.processTextPosition(text);

			Rectangle2D rect = new Rectangle2D.Float(text.getX(), text.getY(), text.getWidth(), -2 * text.getHeight());
			rect = transform(getGraphics().getTransform(), rect);

			annotations.add(new PDFAnnotation(rect, text.getCharacter()));
		}

        protected Rectangle2D transform(AffineTransform at, Rectangle2D rect) {
            Point2D pt1 = new Point2D.Double(rect.getMinX(), rect.getMinY());
            Point2D pt2 = new Point2D.Double(rect.getMaxX(), rect.getMaxY());
            at.transform(pt1, pt1);
            at.transform(pt2, pt2);
            rect.setFrameFromDiagonal(pt1, pt2);
            return rect;
        }

        protected Rectangle2D translate(Rectangle2D rect, Point2D pt) {
            rect.setRect(rect.getX() + pt.getX(), rect.getY() + pt.getY(), rect.getWidth(), rect.getHeight());
            return rect;
        }
    }

    public int getImageCount(String filename) {
        return 1;
    }

    public ImageObject readImage(String filename, int index, SubArea subarea, int sampling) throws IOException, ImageException {
        return readImage(filename, subarea, sampling);
    }

    public ImageObject readImageHeader(String filename, int index) throws IOException, ImageException {
        return readImageHeader(filename);
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
        JMenu me = new JMenu("PDFBox");
        JCheckBoxMenuItem chk = new JCheckBoxMenuItem(new AbstractAction("PDF Annotations") {
            public void actionPerformed(ActionEvent evt) {
                JCheckBoxMenuItem chk = (JCheckBoxMenuItem) evt.getSource();
                setUseAnnotations(chk.isSelected());
            }
        });
        chk.setSelected(true);
        setUseAnnotations(chk.isSelected());
        file.add(prefs);
        prefs.add(me);
        me.add(chk);
        return new JMenuItem[] { file };
    }

    public void imageUpdated(ImageUpdateEvent event) {
    }

    public URL getHelp(String topic) {
        // TODO Auto-generated method stub
        return null;
    }
}
