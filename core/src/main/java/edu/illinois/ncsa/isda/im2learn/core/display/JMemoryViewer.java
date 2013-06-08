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


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

/**
 * Shows the memory usage. This class will show the memory usage of the java
 * process over time. It uses a timer to get the current memory usage every
 * second. The display can be in two modes, show the current memory usage, or
 * show memory usage over time. If set to show over time it will show both
 * memory usage as well as total memory (compared to max memory). If set to show
 * the current memory usage it will show the current memory used of the total
 * memory. Right clicking on the display will force a garbage collect.
 *
 * @author Rob Kooper
 * @version 2.0
 */
public class JMemoryViewer extends JComponent {
    private int len;
    private long[] used;
    private long[] total;
    private Timer timer;
    private boolean showhistory;
    static private Runtime runtime = Runtime.getRuntime();
    static private long mb = 1024 * 1024;
    static private long max = runtime.maxMemory();

    /**
     * Default constructor. Will create a memory viewer with a history of 120
     * values, or 2 minutes.
     */
    public JMemoryViewer() {
        this(120);
    }

    /**
     * Create a memory viewer with the requested number of values for history.
     * This number will also be the width of display.
     *
     * @param history number of values to keep for history.
     */
    public JMemoryViewer(int history) {
        this.len = history;
        this.used = new long[len];
        this.total = new long[len];
        this.showhistory = false;

        timer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.arraycopy(used, 1, used, 0, len-1);
                System.arraycopy(total, 1, total, 0, len-1);
                total[len-1] = runtime.totalMemory();
                used[len-1] = total[len-1] - runtime.freeMemory();
                repaint();
            }
        });
        timer.start();

        setPreferredSize(new Dimension(len, 20));
        setFont(new Font("Dialog", Font.PLAIN, 11));
        setToolTipText("Left click to toggle, Right click to do GC().");

        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    showhistory = !showhistory;
                    repaint();
                } else {
                    System.gc();
                }
            }
        });
    }

    /**
     * Draw the information with a nice border. Based on the value for
     * showhistory either draw the graph as history of usage, or draw the
     * current memory usage.
     *
     * @param g the graphics on which to draw.
     */
    public void paint(Graphics g) {
        super.paint(g);

        int w = getWidth() - 2;
        int h = getHeight() - 2;
        int x = 2;
        int y = 2;

        // fill background
        g.setColor(Color.black);
        g.fillRoundRect(x, y, w, h, 2, 2);

        w -= 4;
        h -= 4;
        x += 2;
        y += 2;

        // draw history or current
        if (showhistory) {
            double scx = (double) w / len;
            double scy = (double) h / max;
            int[] px = new int[len];
            int[] pu = new int[len];
            int[] pt = new int[len];
            for (int i = 0; i < len; i++) {
                px[i] = x + (int) (i * scx);
                pu[i] = y + h - (int) (used[i] * scy);
                pt[i] = y + h - (int) (total[i] * scy);
            }
            g.setColor(Color.green);
            g.drawPolyline(px, pu, len);
            g.setColor(Color.yellow);
            g.drawPolyline(px, pt, len);
        } else {
            // draw the memory usage
            g.setColor(Color.white);
            String str = used[len-1] / mb + "M of " + total[len-1] / mb + "M";
            FontMetrics fm = g.getFontMetrics();
            Rectangle2D rect = fm.getStringBounds(str, g);

            int x1 = x + (w - (int) rect.getWidth()) / 2;
            int y1 = y + (h - (int) rect.getHeight()) / 2 + fm.getAscent();
            g.drawString(str, x1, y1);

            double scx = (double) w / total[len-1];
            int l = (int) (used[len-1] * scx);
            g.setColor(Color.black);
            g.setXORMode(Color.white);
            g.fillRect(x, y, l, h);
        }
    }
}
