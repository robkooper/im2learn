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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.text.DecimalFormat;

import javax.swing.JComponent;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageException;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;

/**
 * Draw a color bar from blue to red. The values that correspond to blue and red
 * can be set as the minimum and maximum values of the color bar.
 * 
 * @author Sang-Chul Lee
 * @author Rob Kooper
 * @version 2.0
 */
public class ColorBar extends JComponent {
    public double            min, max, diff;
    private final float      saturation, brightness;
    private double           zeromark      = Double.NaN;
    private double           zerodelta     = 0;
    private Color            zerocolor     = Color.black;
    private final ColorCache cc            = new ColorCache();

    private static int       colorbarWidth = 20;
    private static Log       logger        = LogFactory.getLog(ColorBar.class);

    /**
     * Create a color bar with minimum value of 0 and maximum of 255.
     */
    public ColorBar() {
        this(0, 255);
    }

    /**
     * Create a colorbar with the give min and max values.
     * 
     * @param min
     *            the minimum value for the range of colors.
     * @param max
     *            the maximum value for the range of colors.
     */
    public ColorBar(double min, double max) {
        this.saturation = 1f;
        this.brightness = 1f;

        setRange(min, max);

        setBackground(Color.white);
        setPreferredSize(new Dimension(100, 250));
    }

    /**
     * Set the range of colors. The colors generated will be between the min
     * color (blue) and max color (red).
     * 
     * @param min
     *            the minimum value for the range of colors.
     * @param max
     *            the maximum value for the range of colors.
     */
    public void setRange(double min, double max) {
        if (max == min) {
            this.max = max + 1;
            this.min = min - 1;
        } else if (max < min) {
            this.max = min;
            this.min = max;
        } else {
            this.max = max;
            this.min = min;
        }
        this.diff = (this.max - this.min);

        cc.clear();
    }

    public void setZeroValue(double val, double delta) {
        zeromark = val;
        zerodelta = delta;
    }

    public double getZeroValue() {
        return zeromark;
    }

    public void setZeroColor(Color val) {
        zerocolor = val;
    }

    public Color getZeroColor() {
        return zerocolor;
    }

    /**
     * Based min and max return the color corresponding to the value. If the
     * value is less than min, the color returned is black. If the color is more
     * that the max value, the color returned is white.
     * 
     * @param val
     *            the color to be looked up.
     * @return the color corresponding to the input value.
     */
    public Color getColor(double val) {
        return getColor(true, val);
    }

    public ImageObject getColorImage(ImageObject src, int band) throws ImageException {
        ImageObject dst = ImageObject.createImage(src.getNumRows(), src.getNumCols(), 3, ImageObject.TYPE_BYTE);
        for (int i = 0, y = 0; y < src.getNumRows(); y++) {
            for (int x = 0; x < src.getNumCols(); x++) {
                double d = src.getDouble(y, x, band);
                Color c = Color.BLACK;
                if (d != src.getInvalidData()) {
                    c = getColor(true, src.getDouble(y, x, band));
                }
                dst.set(i++, c.getRed());
                dst.set(i++, c.getGreen());
                dst.set(i++, c.getBlue());
            }
        }
        return dst;
    }

    private Color getColor(boolean cache, double val) {
        if (!Double.isNaN(zeromark) && (Math.abs(val - zeromark) < zerodelta)) {
            return zerocolor;
        }
        if (val > max) {
            return Color.white;
        } else if (val < min) {
            return Color.black;
        } else if (diff != 0) {
            return cc.get(cache, val);
        } else {
            return Color.green;
        }
    }

    /**
     * Draw the colorbar ranging from min to max.
     * 
     * @param g
     *            graphics to draw the bar on.
     */
    @Override
    protected void paintComponent(Graphics g) {
        int x0 = 5;
        int y0 = 5;

        int x1, x2;
        x1 = x0 + colorbarWidth;
        x2 = x0 + colorbarWidth + 5;

        // find the bounding box of min / max
        FontMetrics fm = g.getFontMetrics();
        // int msg_width = fm.stringWidth (format(max));
        int msg_height = fm.getHeight();

        // draw the min and max values
        g.drawString(format(max), x0, msg_height);
        g.drawString(format(min), x0, getHeight() - y0);

        // draw the colorbar
        int height = getHeight() - y0 - y0 - msg_height - msg_height - 4;
        int y = y0 + 2 + msg_height;
        double v = max;
        double delta = diff / height;
        for (int i = 0; i < height; i++, y++) {
            v -= delta;
            g.setColor(getColor(false, v));
            g.drawLine(x0, y, x1, y);
        }

        // draw a line at zero if asked
        if (!Double.isNaN(zeromark) && (zeromark > min) && (zeromark < max)) {
            g.setColor(zerocolor);
            y = y0 + 2 + msg_height + (int) ((max - zeromark) / delta);
            g.drawLine(x0, y, x1, y);
        }

        // draw Y gridlines
        double dist, step, d, l;
        dist = msg_height * delta;
        step = 5 * dist;
        l = Math.ceil(Math.log(step) / Math.log(10)) - 1;
        l = Math.pow(10, -l);
        step = step * l;
        if (step <= 1) {
            step = 1 / l;
        } else if (step <= 2.5) {
            step = 2.5 / l;
        } else {
            step = 5 / l;
        }

        d = step * Math.ceil((min + dist * 1.5) / step);
        height = (int) (max / delta) + (y0 + 2 + (int) (1.5 * msg_height));
        while (d < max) {
            g.setColor(Color.black);
            g.drawString(format(d), x2, (int) (height - (d / delta)));
            d += step;
        }
    }

    static private DecimalFormat dfExp = new DecimalFormat("0.####E0");
    static private DecimalFormat dfNor = new DecimalFormat("####.###");

    private String format(double d) {
        if (Math.abs(d) < 1e-12) {
            return "0";
        } else if ((Math.abs(d) > 999) || (Math.abs(d) < 0.0001)) {
            return dfExp.format(d);
        } else {
            return dfNor.format(d);
        }
    }

    class ColorCache {
        private final int      max = 1500;
        private final double[] key;
        private final Color[]  color;
        private int            last;
        private int            size;
        private int            chit;
        private int            cmiss;
        private int            count;

        public ColorCache() {
            key = new double[max];
            color = new Color[max];

            clear();
        }

        public void clear() {
            stats();

            last = 0;
            size = 0;
            count = 0;
            chit = 0;
            cmiss = 0;
        }

        public Color get(boolean cache, double val) {
            if (!cache) {
                return get(val);
            }

            int i;
            count++;
            for (i = 0; i < size; i++) {
                if (key[i] == val) {
                    chit++;
                    return color[i];
                }
            }
            cmiss++;

            if (count % 100000 == 0) {
                stats();
            }

            if (size < max) {
                i = size;
                size++;
            } else {
                i = last;
                last++;
                if (last >= max) {
                    last = 0;
                }
            }

            key[i] = val;
            color[i] = get(val);
            return color[i];
        }

        private Color get(double val) {
            float v = (float) ((1.0 - (val - min) / diff) * 0.66666);
            return Color.getHSBColor(v, saturation, brightness);
        }

        public void stats() {
            logger.debug("ColorBar Cache STATS count=" + count + " hits=" + chit + " miss=" + cmiss);
        }
    }
}
