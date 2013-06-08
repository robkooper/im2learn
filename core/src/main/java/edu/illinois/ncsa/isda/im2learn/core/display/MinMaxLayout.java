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

import java.awt.*;

/**
 * Created by IntelliJ IDEA. User: kooper Date: Mar 7, 2004 Time: 10:18:58 AM To
 * change this template use File | Settings | File Templates.
 */
public class MinMaxLayout extends BorderLayout {
    private Dimension minimum = new Dimension(100, 100);
    private Dimension maximum = Toolkit.getDefaultToolkit().getScreenSize();

    public MinMaxLayout() {
        this(0, 0);
    }

    public MinMaxLayout(int hgap, int vgap) {
        super(hgap, vgap);
    }

    public Dimension getMaximum() {
        return maximum;
    }

    public void setMaximum(Dimension maximum) {
        this.maximum = maximum;
    }

    public Dimension getMinimum() {
        return minimum;
    }

    public void setMinimum(Dimension minimum) {
        this.minimum = minimum;
    }

    /**
     * Determines the preferred size of the <code>target</code> container using
     * this layout manager, based on the components in the container.
     * <p/>
     * Most applications do not call this method directly. This method is called
     * when a container calls its <code>getPreferredSize</code> method.
     *
     * @param target the container in which to do the layout.
     * @return the preferred dimensions to lay out the subcomponents of the
     *         specified container.
     * @see java.awt.Container
     * @see java.awt.BorderLayout#minimumLayoutSize
     * @see java.awt.Container#getPreferredSize()
     */
    public Dimension preferredLayoutSize(Container target) {
        return checkSize(super.preferredLayoutSize(target));
    }

    private Dimension checkSize(Dimension dim) {
        if (dim.width < minimum.width) {
            dim.width = minimum.width;
        }
        if (dim.width > maximum.width) {
            dim.width = maximum.width;
        }
        if (dim.height < minimum.height) {
            dim.height = minimum.height;
        }
        if (dim.height > maximum.height) {
            dim.height = maximum.height;
        }
        return dim;
    }
}
