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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObjectByte;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObjectOutOfCore;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;

/**
 * A component that can render ImageObjects. This class will render an
 * imageobject in the center of itself. The component allows the color, the
 * scale, the crop area of the image to be set.
 * <p/>
 * The color of the image is controlled by setting the red, green and blue
 * bands. If the image is set to grayscale the color is controlled by the
 * grayband. Setting fakergb will render a single band image as an RGB image. To
 * brigthen or darken the image, the gamma factor can be adjusted.
 * <p/>
 * Images other than byte need to be converted to type byte to be rendered.
 * During this process either the minimum and maximum values per band can be
 * used, resulting in images with maximal dynamic range per band, or absolute
 * minimum and maximum can be used, resulting in uniform images.
 * <p/>
 * The size of the image can be controlled using zoom. If the component is set
 * to autozoom, the image will take up as much space as possible. If the
 * component is set to a fixed zoom factor, the image will be scaled according
 * to the zoom factor. If the image is larger than the component, only the upper
 * left corner will be visible. To overcome this, the component can be placed
 * inside a JScrollPane.
 * <p/>
 *
 * @author Rob Kooper
 * @version 1.0
 */
public class ImageComponent extends JComponent implements Scrollable, Printable {
    private boolean autozoom = false;
    private boolean fakergb = false;
    private boolean useTotals = false;
    private double zoomfactor = 1.0;
    transient private BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_GRAY);
    private ImageObject imageobject = null;
    private Rectangle crop = new Rectangle();
    private int redBand = 0;
    private int greenBand = 1;
    private int blueBand = 2;
    private int grayBand = 0;
    private boolean grayscale = false;
    private double gamma = 1.0;
    private int[] gammatable = null;
    private Vector listeners = new Vector();
    private int imagex = 0;
    private int imagey = 0;
    private Dimension preferredsize = null;
    private double[] scale = new double[]{0, 1};
    private boolean userScale = false;
    private Rectangle visregion = new Rectangle();
    private double[] imagescale = new double[2];
    private double[] oneoverimagescale = new double[2];
    private double paintscale = 1.0;

    transient private ResizeComponentListener resizer;

    private static Log logger = LogFactory.getLog(ImageComponent.class);

    /**
     * ImageObject used if no image is specified.
     */
    private static ImageObject dummy = new ImageObjectByte(1, 1, 1);

    /**
     * Default size of the panel is 640x480
     */
    private Dimension panelsize = new Dimension(640, 480);
    
    static {
    	// prevent dummy from showing up in multimagepanel
    	dummy.setProperty("_MIP", dummy.hashCode());
    }

    /**
     * Default constructor, creates imagecomponent with no image displayed.
     */
    public ImageComponent() {
        this(null);
    }

    /**
     * Craetes imagecomponent that will display the imageobject.
     *
     * @param imageobject that is displayed in this imagecomponent.
     */
    public ImageComponent(ImageObject imageobject) {
        resizer = new ResizeComponentListener();

        addPropertyChangeListener("ancestor", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getOldValue() instanceof JViewport) {
                    JViewport view = (JViewport) evt.getOldValue();
                    view.removeComponentListener(resizer);
                }
                if (evt.getNewValue() instanceof JViewport) {
                    JViewport view = (JViewport) evt.getNewValue();
                    view.addComponentListener(resizer);
                }
            }
        });

        addComponentListener(resizer);

        // show image
        setImageObject(imageobject);
    }

    /**
     * Returns the current band used for the green color when displaying the
     * image as RGB. For more information see setRedBand().
     *
     * @return the band shown as red if image is displayed as RGB.
     */
    public int getRedBand() {
        return redBand;
    }

    /**
     * Sets which band should be used for red if the image is rendered as RGB.
     * If the image is currently being shown as RGB the image is immediatly
     * updated to reflect this change. After changing the value and repainting
     * all imageupdate listeneres will be notified with the new red band as an
     * Integer.
     *
     * @param redBand the new band to use for red when showing the image as
     *                RGB.
     * @throws IllegalArgumentException if the band is less than 0, or larger
     *                                  than the number of bands in the image.
     */
    public void setRedBand(int redBand) throws IllegalArgumentException {
        if ((redBand < -1) || (redBand >= imageobject.getNumBands())) {
            throw(new IllegalArgumentException("redBand less than 0 or larger than number of bands."));
        }
        if (this.redBand == redBand) {
            return;
        }
        this.redBand = redBand;
        if (!grayscale) {
            makeImage();
            repaint();
        }
        fireImageUpdate(ImageUpdateEvent.CHANGE_REDBAND, new Integer(redBand));
    }

    /**
     * Returns the current band used for the green color when displaying the
     * image as RGB. For more information see setGreenBand().
     *
     * @return the band shown as green if image is displayed as RGB.
     */
    public int getGreenBand() {
        return greenBand;
    }

    /**
     * Sets which band should be used for green if the image is rendered as RGB.
     * If the image is currently being shown as RGB the image is immediatly
     * updated to reflect this change. After changing the value and repainting
     * all imageupdate listeneres will be notified with the new green band as an
     * Integer.
     *
     * @param greenBand the new band to use for green when showing the image as
     *                  RGB.
     * @throws IllegalArgumentException if the band is less than 0, or larger
     *                                  than the number of bands in the image.
     */
    public void setGreenBand(int greenBand) throws IllegalArgumentException {
        if ((greenBand < -1) || (greenBand >= imageobject.getNumBands())) {
            throw(new IllegalArgumentException("greenBand less than 0 or larger than number of bands."));
        }
        if (this.greenBand == greenBand) {
            return;
        }
        this.greenBand = greenBand;
        if (!grayscale) {
            makeImage();
            repaint();
        }
        fireImageUpdate(ImageUpdateEvent.CHANGE_GREENBAND, new Integer(greenBand));
    }

    /**
     * Returns the current band used for the blue color when displaying the
     * image as RGB. For more information see setBlueBand().
     *
     * @return the band shown as blue if image is displayed as RGB.
     */
    public int getBlueBand() {
        return blueBand;
    }

    /**
     * Sets which band should be used for blue if the image is rendered as RGB.
     * If the image is currently being shown as RGB the image is immediatly
     * updated to reflect this change. After changing the value and repainting
     * all imageupdate listeneres will be notified with the new blue band as an
     * Integer.
     *
     * @param blueBand the new band to use for blue when showing the image as
     *                 RGB.
     * @throws IllegalArgumentException if the band is less than 0, or larger
     *                                  than the number of bands in the image.
     */
    public void setBlueBand(int blueBand) throws IllegalArgumentException {
        if ((blueBand < -1) || (blueBand >= imageobject.getNumBands())) {
            throw(new IllegalArgumentException("blueBand less than 0 or larger than number of bands."));
        }
        if (this.blueBand == blueBand) {
            return;
        }
        this.blueBand = blueBand;
        if (!grayscale) {
            makeImage();
            repaint();
        }
        fireImageUpdate(ImageUpdateEvent.CHANGE_BLUEBAND, new Integer(blueBand));
    }

    /**
     * Sets which bands should be used for red, green and blue if the image is
     * rendered as RGB. If the image is currently being shown as RGB the image
     * is immediatly updated to reflect this change. After changing the value
     * and repainting all imageupdate listeneres will be notified with the new
     * red, green and blue bands as an int array.
     *
     * @param redBand   the new band to use for red when showing the image as
     *                  RGB.
     * @param greenBand the new band to use for green when showing the image as
     *                  RGB.
     * @param blueBand  the new band to use for blue when showing the image as
     *                  RGB.
     * @throws IllegalArgumentException if the band is less than 0, or larger
     *                                  than the number of bands in the image.
     */
    public void setRGBBand(int redBand, int greenBand, int blueBand) throws IllegalArgumentException {
        if ((redBand < -1) || (blueBand >= imageobject.getNumBands())) {
            throw(new IllegalArgumentException("redBand less than 0 or larger than number of bands."));
        }
        if ((greenBand < -1) || (greenBand >= imageobject.getNumBands())) {
            throw(new IllegalArgumentException("greenBand less than 0 or larger than number of bands."));
        }
        if ((blueBand < -1) || (blueBand >= imageobject.getNumBands())) {
            throw(new IllegalArgumentException("blueBand less than 0 or larger than number of bands."));
        }
        if ((this.redBand == redBand) && (this.greenBand == greenBand) && (this.blueBand == blueBand)) {
            return;
        }
        this.redBand = redBand;
        this.greenBand = greenBand;
        this.blueBand = blueBand;
        if (!grayscale) {
            makeImage();
            repaint();
        }
        fireImageUpdate(ImageUpdateEvent.CHANGE_RGBBAND, new int[]{redBand, greenBand, blueBand});
    }

    /**
     * Returns the current band used when displaying the image as grayscale. For
     * more information see setGrayBand().
     *
     * @return the band shown if image is displayed as grayscale.
     */
    public int getGrayBand() {
        return grayBand;
    }

    /**
     * Sets which band should be used if the image is rendered as grayscale. If
     * the image is currently being shown as grayscale the image is immediatly
     * updated to reflect this change. After changing the value and repainting
     * all imageupdate listeneres will be notified with the new gray band as an
     * Integer.
     *
     * @param grayBand the new band to use when showing the image as grayscale.
     * @throws IllegalArgumentException if the band is less than 0, or larger
     *                                  than the number of bands in the image.
     */
    public void setGrayBand(int grayBand) throws IllegalArgumentException {
        if ((grayBand < 0) || (grayBand >= imageobject.getNumBands())) {
            throw(new IllegalArgumentException("grayBand less than 0 or larger than number of bands."));
        }
        if (this.grayBand == grayBand) {
            return;
        }
        this.grayBand = grayBand;
        if (grayscale) {
            makeImage();
            repaint();
        }
        fireImageUpdate(ImageUpdateEvent.CHANGE_GRAYBAND, new Integer(grayBand));
    }

    /**
     * Returns whether a single band of the image is currently rendered. For
     * more information see setGrayScale().
     *
     * @return true if only a single band of the image rendered.
     */
    public boolean isGrayScale() {
        return grayscale;
    }

    /**
     * Set this to true to use the grayband as a single band of the image. This
     * allows the user to see a single band of the image, if the image has only
     * one band this will always be true. After changing the value and
     * repainting all imageupdate listeneres will be notified with the new
     * grayscale as a Boolean.
     *
     * @param grayscale set this to true to render a single band of the image.
     */
    public void setGrayScale(boolean grayscale) {
        if (imageobject.getNumBands() == 1) {
            return;
        }
        if (this.grayscale == grayscale) {
            return;
        }
        this.grayscale = grayscale;
        makeImage();
        repaint();
        fireImageUpdate(ImageUpdateEvent.CHANGE_GRAYSCALE, Boolean.valueOf(grayscale));
    }

    /**
     * Returns the current gamma value. For more information see setGamma().
     *
     * @return the current gamma value.
     */
    public double getGamma() {
        return 1 / gamma;
    }

    /**
     * Sets the gamma value used when rendering the image. Gamma values less
     * than 1.0 will darken the image allowing the user to see any details in
     * oversaturated areas. Setting this to a value larger than 1.0 will
     * brighten the image allowing the user to see details in dark areas. After
     * changing the value and repainting all imageupdate listeneres will be
     * notified with the new gamma as a Double.
     *
     * @param gamma the gamma value to use.
     * @throws IllegalArgumentException if the gamma is 0 or less.
     */
    public void setGamma(double gamma) throws IllegalArgumentException {
        if (gamma <= 0) {
            throw(new IllegalArgumentException("Gamma should not be 0 or smaller."));
        }
        if (this.gamma == 1 / gamma) {
            return;
        }
        this.gamma = 1 / gamma;
        if (gamma == 1.0) {
        	gammatable = null;
        } else {
        	if(gammatable == null){
        		gammatable = new int[256];
        	}
	        for (int i = 0; i < 256; i++) {
	            gammatable[i] = (int) (255 * Math.pow(i / 255.0, this.gamma));
	            if (gammatable[i] > 255)
	                gammatable[i] = 255;
	        }
        }
        makeImage();
        repaint();
        fireImageUpdate(ImageUpdateEvent.CHANGE_GAMMA, new Double(gamma));
    }

    /**
     * Returns whether the image is using autozoom. For more information see
     * setAutozoom().
     *
     * @return true if the image is in autozoom.
     */
    public boolean isAutozoom() {
        return autozoom;
    }

    /**
     * Sets the image to autozoom or to normal zoom. If the image is set to
     * autozoom this function will immediatly calculate the new zoomfactor and
     * repaint the image. After changing the value and repainting all
     * imageupdate listeneres will be notified with the new autozoom as a
     * Boolean.
     *
     * @param autozoom set this to true to automatically zoom the image to the
     *                 maximum space available.
     */
    public void setAutozoom(boolean autozoom) {
        if (autozoom == this.autozoom) {
            return;
        }
        this.autozoom = autozoom;
        if (autozoom) {
            calcLocation(0, 0, panelsize, false);
            revalidate();
            repaint();
        }
        fireImageUpdate(ImageUpdateEvent.CHANGE_AUTOZOOM, Boolean.valueOf(autozoom));
    }

    /**
     * Returns the current zoomfactor.  If the image is using autzoom this will
     * be the zoomfactor as calculated by the autozoom, if no autozoom is used
     * this will be the last zoomfactor set.
     *
     * @return the current zoomfactor.
     */
    public double getZoomFactor() {
        return zoomfactor;
    }

    /**
     * Set the new zoomfactor for the image. If the image was using autozoom
     * before this will be disabled and the new zoomfactor will be used. After
     * changing the value and repainting all imageupdate listeneres will be
     * notified with the new zoomfactor as a Double.
     *
     * @param zoomfactor the new zoomfactor to use.
     * @throws IllegalArgumentException if the zoomfactor is 0 or less.
     */
    public void setZoomFactor(double zoomfactor) throws IllegalArgumentException {
        if (zoomfactor <= 0) {
            throw(new IllegalArgumentException("Zoom should not be 0 or smaller."));
        }
        this.autozoom = false;
        if (this.zoomfactor == zoomfactor) {
            return;
        }
        this.zoomfactor = zoomfactor;
        revalidate();
        calcLocation(0, 0, panelsize, false);
        repaint();
        fireImageUpdate(ImageUpdateEvent.CHANGE_ZOOMFACTOR, new Double(zoomfactor));
    }
    
    public double getPaintScale() {
    	return paintscale;
    }
    
    public void setPaintScale(double ps) {
    	if ((ps <= 0) || (ps > 1)) {
            throw(new IllegalArgumentException("PaintScale should be larger than 0 and less or equal to 1."));
    	}
    	if (ps == paintscale) {
    		return;
    	}
    	paintscale = ps;
        makeImage();
        repaint();
        fireImageUpdate(ImageUpdateEvent.CHANGE_PAINTSCALE, new Double(paintscale));
    }

    /**
     * Returns true if the absolute minimum and maximum of the image are used to
     * show the image. For more details see setUseTotals().
     *
     * @return true if the maximum value in the image should be used.
     */
    public boolean isUseTotals() {
        return useTotals;
    }

    /**
     * Use absolute maximum when showing image. If this is true, the colors
     * calculated for the red, green and blue band will us the maximum value
     * found in the image to scale the values. If this is false it will use the
     * maximum value per band to scale the image, often this will result in an
     * image where the values in the blue band are scaled as compared to those
     * in the red band.
     * <p/>
     * If the image is of type BYTE, this will be ignored since no scaling is
     * performed.
     * <p/>
     * After changing the value and repainting all imageupdate listeneres will
     * be notified with the new usetotals as a Boolean.
     *
     * @param useTotals is true if the maximum value in the image should be
     *                  used.
     */
    public void setUseTotals(boolean useTotals) {
        if (this.useTotals == useTotals) {
            return;
        }
        this.useTotals = useTotals;
        makeImage();
        revalidate();
        repaint();
        fireImageUpdate(ImageUpdateEvent.CHANGE_USETOTALS, Boolean.valueOf(useTotals));
    }

    /**
     * Returns true if the user set the min and max values to use. If this is
     * false the code will find the minimum and maximum values in the image and
     * scale all values with respect to these to fit the full color spectrum.
     * Otherwise it will use the user specified minimum and maximum values.
     *
     * @return true if the user has specified the minumum and maximum values.
     */
    public boolean isUserScale() {
        return userScale;
    }

    /**
     * If this is false the code will find the minimum and maximum values in the
     * image and scale all values with respect to these to fit the full color
     * spectrum. Otherwise it will use the user specified minimum and maximum
     * values.
     *
     * @param userScale set this to true to use the user specified minimum and
     *                  maximum.
     */
    public void setUserScale(boolean userScale) {
        if (this.userScale == userScale) {
            return;
        }
        this.userScale = userScale;
        makeImage();
        revalidate();
        repaint();
        fireImageUpdate(ImageUpdateEvent.CHANGE_USERSCALE, Boolean.valueOf(userScale));
    }

    /**
     * Sets the user minimum and maximum scale. These values are used to scale
     * all values in the image such that the image fills the whole
     * colorspectrum. Any values that fall outside the minimum and maximum will
     * be clipped. Normally the code will find the best minimum and maximum to
     * use.
     *
     * @param min the minimum value in the image.
     * @param max the maximum value in the image.
     */
    public void setUserScale(double min, double max) {
        this.userScale = true;
        this.scale[0] = min;
        this.scale[1] = max;
        makeImage();
        revalidate();
        repaint();
        fireImageUpdate(ImageUpdateEvent.CHANGE_USERSCALEVALUE, new double[]{min, max});
    }

    /**
     * Returns the user scale. This will return minimum (result[0]) and maximum
     * (result[1]) values as set by the user.
     *
     * @return an array with the minimum and maximum values specified by the
     *         user.
     */
    public double[] getUserScale() {
        return scale;
    }

    /**
     * Return the Color based on the current settings of the image. This will
     * take the gray color passed in and convert the gray color to the
     * appropriate color for the image. This takes into account gamma, grayband
     * selectected and the minimum and maximum values of the image, or those set
     * by the user.
     *
     * @param gray the gray value to convert to a color.
     * @return the Color that corresponds to the gray value.
     */
    public Color getColor(double gray) {
        int g;
        double min, max, div;

        g = 0;

        int numbands = imageobject.getNumBands();
        int k = (grayBand < 0 || grayBand >= numbands) ? -1 : grayBand;

        if (userScale) {
            min = scale[0];
            max = scale[1];
        } else {
            min = imageobject.getMin(useTotals || (k < 0) ? numbands : k);
            max = imageobject.getMax(useTotals || (k < 0) ? numbands : k);
        }
        div = 255.0 / (max - min);

        if (k >= 0) {
            if (gray <= min) {
                gray = min;
            } else if (gray >= max) {
                gray = max;
            }
            gray = (gray - min) * div;
        	g = (int)gray & 0xff;
            if (gammatable != null) {
            	g = gammatable[g];
            }
        }

        return new Color(g, g, g);
    }

    /**
     * Return the Color based on the current settings of the image. This will
     * take the red, green and blue color passed in and convert the colors to
     * the appropriate color for the image. This takes into account gamma,
     * redband, greenband, blueband selectected and the minimum and maximum
     * values of the image, or those set by the user.
     *
     * @param red   the red value to convert to a color.
     * @param green the green value to convert to a color.
     * @param blue  the blue value to convert to a color.
     * @return the Color that corresponds to the red, green and blue values.
     */
    public Color getColor(double red, double green, double blue) {
        int r, g, b;
        double min[] = new double[3];
        double max[] = new double[3];
        double div[] = new double[3];

        int numbands = imageobject.getNumBands();
        int k = (redBand < 0 || redBand >= numbands) ? -1 : redBand;
        int l = (greenBand < 0 || greenBand >= numbands) ? -1 : greenBand;
        int m = (blueBand < 0 || blueBand >= numbands) ? -1 : blueBand;

        r = 0;
        g = 0;
        b = 0;

        if (userScale) {
            min[0] = scale[0];
            max[0] = scale[1];
            min[1] = scale[0];
            max[1] = scale[1];
            min[2] = scale[0];
            max[2] = scale[1];
        } else {
            min[0] = imageobject.getMin(useTotals || (k < 0) ? numbands : k);
            max[0] = imageobject.getMax(useTotals || (k < 0) ? numbands : k);
            min[1] = imageobject.getMin(useTotals || (l < 0) ? numbands : l);
            max[1] = imageobject.getMax(useTotals || (l < 0) ? numbands : l);
            min[2] = imageobject.getMin(useTotals || (m < 0) ? numbands : m);
            max[2] = imageobject.getMax(useTotals || (m < 0) ? numbands : m);
        }
        div[0] = 255.0 / (max[0] - min[0]);
        div[1] = 255.0 / (max[1] - min[1]);
        div[2] = 255.0 / (max[2] - min[2]);

        if (k >= 0) {
            if (red <= min[0]) {
                red = min[0];
            } else if (red >= max[0]) {
                red = max[0];
            }
            red = (red - min[0]) * div[0];
        	r = (int) red & 0xff;
            if (gammatable != null) {
            	r = gammatable[r];
            }            
        }
        if (l >= 0) {
            if (green <= min[0]) {
                green = min[0];
            } else if (green >= max[0]) {
                green = max[0];
            }
            green = (green - min[0]) * div[0];
            g = (int) green & 0xff;
            if (gammatable != null) {
            	g = gammatable[g];
            }
        }
        if (m >= 0) {
            if (blue <= min[0]) {
                blue = min[0];
            } else if (blue >= max[0]) {
                blue = max[0];
            }
            blue = (blue - min[0]) * div[0];
            b = (int) blue & 0xff;
            if (gammatable != null) {
            	b = gammatable[b];
            }
        }

        return new Color(r, g, b);
    }

    /**
     * Returns true if the image is using fakergb. For more information see
     * setFakeRGBcolor().
     *
     * @return true if the image is using pseudocolors.
     */
    public boolean isFakeRGBcolor() {
        return fakergb;
    }

    /**
     * Setting fakergb to true will split the imagevalue over red green and
     * blue. If the image only contains a single band it is often useful to take
     * this single value and split it. After changing the value and repainting
     * all imageupdate listeneres will be notified with the new fakergb as a
     * Boolean.
     *
     * @param pseudocolor set this to true to use pseudocolors.
     */
    public void setFakeRGBcolor(boolean pseudocolor) {
        if (this.fakergb == pseudocolor) {
            return;
        }
        this.fakergb = pseudocolor;
        makeImage();
        revalidate();
        repaint();
        fireImageUpdate(ImageUpdateEvent.CHANGE_PSEUDOCOLOR, Boolean.valueOf(pseudocolor));
    }

    /**
     * Return the area of the image that is shown. It is possible for the user
     * to select an area of the image to be shown. This call will return the
     * currently selected area.
     *
     * @return area of the image currently shown.
     */
    public Rectangle getCrop() {
        if ((crop.width == imageobject.getNumCols()) && (crop.height == imageobject.getNumRows())) {
            return null;
        } else {
            return new Rectangle(crop);
        }
    }

    /**
     * Set the area of the image to be shown. If this is called with null the
     * whole image is shown, otherwise only the area selected it shown. This
     * will not limit the bands that can be shown, just the area in each band.
     * After changing the value and repainting all imageupdate listeneres will
     * be notified with the new crop.
     *
     * @param crop the area to show.
     */
    public void setCrop(Rectangle2D crop) {
        if (crop == null) {
            this.crop.x = 0;
            this.crop.y = 0;
            this.crop.width = imageobject.getNumCols();
            this.crop.height = imageobject.getNumRows();
        } else {
            this.crop.setRect(crop);
        }
        calcLocation(0, 0, panelsize, false);
        revalidate();
        repaint();
        fireImageUpdate(ImageUpdateEvent.CHANGE_CROP, this.crop);
    }

    /**
     * Return the imageobject currently shown. If setImageObject is called with
     * null, this will return null as well.
     *
     * @return imageobject currently shown.
     */
    public ImageObject getImageObject() {
        if (imageobject.equals(dummy)) {
            return null;
        }
        return imageobject;
    }

    /**
     * Set the image to be displayed. This function will calculate the min and
     * max values used displaying the image as well as the coefficient needed to
     * convert the image from any other type to bytes. If this is called with
     * null a 1x1x1 imageobject is used to render to the screen. After changing
     * the value and repainting all imageupdate listeneres will be notified with
     * the new imageobject.
     *
     * @param imageobject to be displayed
     */
    public void setImageObject(ImageObject imageobject) {
        // in case of a null object use the dummy 1x1x1 object.
        if (imageobject == null) {
            imageobject = dummy;
        }

        // a new image
        this.imageobject = imageobject;

        // check for default r, g, b
        redBand = 0;
        greenBand = 1;
        blueBand = 2;
        int[] rgb = (int[]) imageobject.getProperty(ImageObject.DEFAULT_RGB);
        if (rgb != null) {
            if ((rgb[0] >= 0) && (rgb[0] < imageobject.getNumBands())) {
                redBand = rgb[0];
            }
            if ((rgb[1] >= 0) && (rgb[1] < imageobject.getNumBands())) {
                greenBand = rgb[1];
            }
            if ((rgb[2] >= 0) && (rgb[2] < imageobject.getNumBands())) {
                blueBand = rgb[2];
            }
        }

        // check for default grayscale values.
        grayBand = 0;
        int[] gray = (int[]) imageobject.getProperty(ImageObject.DEFAULT_GRAY);
        if (gray != null) {
            if ((gray[0] >= 0) && (gray[0] < imageobject.getNumBands())) {
                grayBand = gray[0];
            }
        }

        // check for default gamma value, if none use 1.0
        double[] gamma = (double[]) imageobject.getProperty(ImageObject.DEFAULT_GAMMA);
        if ((gamma != null) && (gamma[0] != 1.0)) {
            this.gamma = 1 / gamma[0];
            for (int i = 0; i < 256; i++) {
                gammatable[i] = (int) (255 * Math.pow(i / 255.0, this.gamma));
                if (gammatable[i] > 255)
                    gammatable[i] = 255;
            }
        } else {
            this.gamma = 1.0;
            gammatable = null;
        }

        // in the case of a single band switch to grayscale mode.
        if (imageobject.getNumBands() == 1) {
            grayscale = true;
        } else {
            grayscale = false;
        }

        // undo any crop leftovers
        crop.width = imageobject.getNumCols();
        crop.height = imageobject.getNumRows();
        crop.x = 0;
        crop.y = 0;

        // undo fakergb selection
        fakergb = ((imageobject.getType() == ImageObject.TYPE_INT) &&
        		   (imageobject.getNumBands() == 1));
        
        // set the paintscale
        if (imageobject instanceof ImageObjectOutOfCore) {
        	paintscale = ((ImageObjectOutOfCore)imageobject).getSuggestedScale();
        } else {
        	paintscale = 1.0;
        }
        // if imageobject instanceof OOC) paintscale = imageobject.getInCoreScale(); else 1.

        // create image to be rendered
        makeImage();

        // Calculate the location of the image.
        calcLocation(0, 0, panelsize, false);

        // paint
        revalidate();
        repaint();
        fireImageUpdate(ImageUpdateEvent.NEW_IMAGE, imageobject);
    }

    /**
     * Create a buffered image from a ImageObject. Based on the properties set
     * in this component convert the ImageObject to a BufferedImage. This
     * BufferedImage can then be painted on the imagecomponent.
     */
    public void makeImage() {
        // start the wait cursor
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        // create the new bufferedimage
        synchronized (getTreeLock()) {
            //System.out.println("Fake RGB : " + fakergb);
            image = imageobject.toBufferedImage(image, fakergb,
                                                (userScale ? scale : null), useTotals,
                                                redBand, greenBand, blueBand,
                                                grayscale, grayBand,
                                                gammatable, 255, paintscale);
            
            imagescale[0] = (double)imageobject.getNumCols() / image.getWidth(); 
            imagescale[1] = (double)imageobject.getNumRows() / image.getHeight(); 
            oneoverimagescale[0] = 1.0 / imagescale[0]; 
            oneoverimagescale[1] = 1.0 / imagescale[1]; 
        }

        // show normal cursor
        setCursor(Cursor.getDefaultCursor());
    }

    /**
     * This will draw the image with regards to the previously calculate zoom
     * and image location.
     */
    public void paintComponent(Graphics g) {
    	Graphics2D g2d = (Graphics2D)g;
        synchronized (getTreeLock()) {
        	g2d.setClip(getVisibleRect());
            setupGraphics(g2d);
            
            // scale for image
        	g2d.scale(imagescale[0], imagescale[1]);
        	g2d.drawImage(image, 0, 0, null);
        	g2d.scale(oneoverimagescale[0], oneoverimagescale[1]);

            Rectangle curclip = g2d.getClipBounds();
            if (!curclip.equals(visregion)) {
                fireImageUpdate(ImageUpdateEvent.CHANGE_VISIBLEREGION, curclip);
                visregion = curclip;                
            }
        }
    }
    
    /**
     * Returns the region of the image that is visible in image coordinates.
     * 
     * @return rectangle in image coordinates of the visible region.
     */
    public Rectangle getVisibleRegion() {
        return visregion;
    }

    /**
     * Return the image that is currently drawn on the screen. This image will
     * be the representation of the ImageObject with gamma, selected bands, etc.
     * applied to it.
     *
     * @return The currently shown image.
     */
    public BufferedImage getImage() {
        return image;
    }

    /**
     * Apply zoom, translation and crop to graphics object. Use this function to
     * setup the graphics after calling getGraphics() or createGraphics().
     *
     * @param g2d graphics that needs to match the painted graphics.
     */
    public void setupGraphics(Graphics2D g2d) {    	
        g2d.translate(imagex, imagey);
        g2d.scale(zoomfactor, zoomfactor);
        g2d.translate(-crop.x, -crop.y);
        g2d.clipRect(crop.x, crop.y, crop.width, crop.height);
    }

    /**
     * Returns the location of the point in image coordinates, this will take
     * the zoom and translation into account.
     *
     * @param e the point tp be checked.
     */
    public Point getImageLocation(Point e) {
        int x = crop.x + (int) Math.floor((e.getX() - imagex) / zoomfactor);
        int y = crop.y + (int) Math.floor((e.getY() - imagey) / zoomfactor);

        return new Point(x, y);
    }

    /**
     * Set the preferredsize of the imagecomponent. This will prevent the
     * component from calculating its preferred size and simply use what the
     * user has given.
     *
     * @param preferredSize the preferred size of the component.
     */
    public void setPreferredSize(Dimension preferredSize) {
        preferredsize = preferredSize;
        super.setPreferredSize(preferredSize);
    }

    /**
     * Calculate the preferred size based on zoomfactor and crop. If the parent
     * is a viewport make sure that the minimal size is the size of the
     * viewport. This will make sure that even in a scrollable pane we are
     * centered. If the user has set the preferredsize simply return this.
     *
     * @return size based on crop and zoomfactor.
     */
    public Dimension getPreferredSize() {
        if ((preferredsize != null) && (autozoom || (zoomfactor == 1.0))) {
            return preferredsize;
        }

        Dimension size = new Dimension((int) (crop.width * zoomfactor),
                                       (int) (crop.height * zoomfactor));

        if (getParent() instanceof JViewport) {
            Dimension parent = getParent().getSize();
            if (parent.width > size.width) {
                size.width = parent.width;
            }
            if (parent.height > size.height) {
                size.height = parent.height;
            }
        }

        return size;
    }

    /**
     * Calculate the new scale, if autozoom, and location of the image such that
     * it is centered in the component and in the case of autozoom as large as
     * possible. If the zoomfactor changes all imageupdate listeners will be
     * notified with the new zoomfactor as a Double.
     *
     * @param x     number of pixels added to the x location.
     * @param y     number of pixels added to the y location.
     * @param size  the size that the imagecomponent can take up.
     * @param print is true if this is called from the true function and no
     *              events are broadcasted if zoom changes.
     */
    protected void calcLocation(int x, int y, Dimension size, boolean print) {
        // if autozoom, calculate the zoom
        if (autozoom) {
            float sx = (float)size.width / crop.width;
            float sy = (float)size.height / crop.height;
            float sc = (sx < sy) ? sx : sy;

            if (this.zoomfactor != sc) {
                this.zoomfactor = sc;
                if (!print) {
                    fireImageUpdate(ImageUpdateEvent.CHANGE_ZOOMFACTOR, new Double(sc));
                }
            }
        }
        
        // center image based on imagesize and available screenspace
        Dimension imgsize = new Dimension((int) (crop.width * zoomfactor),
                                          (int) (crop.height * zoomfactor));

        // calculate center of screen adding user offset
        imagex = x;
        if (size.width >= imgsize.width) {
            imagex += (size.width - imgsize.width) / 2;
        }
        imagey = y;
        if (size.height >= imgsize.height) {
            imagey += (size.height - imgsize.height) / 2;
        }
    }

    // -----------------------------------------------------------------------
    // IMAGEUPDATE LISTENER
    // -----------------------------------------------------------------------
    /**
     * Add the specified image update listener to receive image update events
     * from this component. If l is null no exception is thrown and no action is
     * performed.
     *
     * @param l the image update listener
     */
    public void addImageUpdateListener(ImageUpdateListener l) {
        if ((l != null) && !listeners.contains(l)) {
            listeners.add(l);
        }
    }

    /**
     * Removes the specified image update listener so that it no longer receives
     * image update events from this component. This method performs no
     * function, nor does it throw an exception, if the listener specified by
     * the argument was not previously added to this component. If listener l is
     * null, no exception is thrown and no action is performed.
     *
     * @param l the image update listener
     */
    public void removeImageUpdateListener(ImageUpdateListener l) {
        if ((l != null) && listeners.contains(l)) {
            listeners.remove(l);
        }
    }

    /**
     * Support for reporting image property changes . This method can be called
     * when an image property has changed and it will send the appropriate
     * ImageUpdateEvent to any registered ImageUpdateListener.
     *
     * @param id the reason this property is fired.
     */
    protected void fireImageUpdate(int id, Object obj) {
        ImageUpdateEvent event = new ImageUpdateEvent(this, id, obj);
        for (int i = 0; i < listeners.size(); i++) {
            try {
                ((ImageUpdateListener) listeners.get(i)).imageUpdated(event);
            } catch (Throwable thr) {
                logger.info("Uncaught exception", thr);
            }
        }
    }

    // -----------------------------------------------------------------------
    // COMPONENTLISTENER
    // -----------------------------------------------------------------------
    /**
     * Listens for resize events.
     */
    class ResizeComponentListener extends ComponentAdapter {
        /**
         * If the component is resized, recalculate the zoomfactor and the
         * location of the image.
         */
        public void componentResized(ComponentEvent e) {
            // what is the drawing size.
            panelsize = getSize(panelsize);

            // special case if inside scrollpane
            if (getParent() instanceof JViewport) {
                panelsize = getParent().getSize(panelsize);
            }

            // make sure it is not 0
            if (panelsize.width == 0) {
                panelsize.width = imageobject.getNumCols();
            }
            if (panelsize.height == 0) {
                panelsize.height = imageobject.getNumRows();
            }

            // zoom and location
            calcLocation(0, 0, panelsize, false);

            // redraw in the center
            revalidate();
            repaint();
        }
    }

    // -----------------------------------------------------------------------
    // IMPLEMENTATION OF SCROLLABLE INTERFACE
    // -----------------------------------------------------------------------
    /**
     * Return as big as possible size, we will center image in center of pane if
     * it is larger than the space available.
     */
    public Dimension getPreferredScrollableViewportSize() {
        // the size of the viewport should be the preferred size, we already
        // take care of sizing it as big as possible there.
        return getPreferredSize();
    }

    /**
     * If autozoom we do track the size of the viewport and scale accordingly.
     */
    public boolean getScrollableTracksViewportHeight() {
        return autozoom;
    }

    /**
     * If autozoom we do track the size of the viewport and scale accordingly.
     */
    public boolean getScrollableTracksViewportWidth() {
        return autozoom;
    }

    /**
     * Scroll a single pixel in the direction of the arrow that is clicked on.
     */
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 1; // 1 pixel at a time
    }

    /**
     * If clicked on the scrollbar, scroll a full page in that direction.
     */
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        // scroll one full page at a time
        if (orientation == SwingConstants.VERTICAL) {
            return visibleRect.height;
        } else {
            return visibleRect.width;
        }
    }

    // -----------------------------------------------------------------------
    // IMPLEMENTATION OF PRINTABLE INTERFACE
    // -----------------------------------------------------------------------
    /**
     * Print the image. This will resize the currently selected area of the
     * image so that it fills as much of the paper as possible. It will also
     * center the image in the center of the page.
     */
    public int print(Graphics g, PageFormat pf, int pi) throws PrinterException {
        if (pi == 0) {
            int oldx = imagex;
            int oldy = imagey;
            double oldzoom = zoomfactor;

            // setup paper size
            Dimension size = new Dimension((int) pf.getImageableWidth(),
                                           (int) pf.getImageableHeight());

            // calculate zoom and location
            calcLocation((int) pf.getImageableX(), (int) pf.getImageableY(),
                         size, true);

            // paint/print
            paint(g);

            // reset
            imagex = oldx;
            imagey = oldy;
            zoomfactor = oldzoom;

            // done
            return PAGE_EXISTS;
        } else {
            return NO_SUCH_PAGE;
        }
    }
}
