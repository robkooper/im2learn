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
package edu.illinois.ncsa.isda.imagetools.ext.misc;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageException;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA. User: kooper Date: Oct 11, 2004 Time: 5:18:31 PM To
 * change this template use File | Settings | File Templates.
 */
public class PlotComponent extends JComponent {
    private ArrayList dataseries = new ArrayList();
    private int maxid = 0;

    private boolean autoX = true;
    private boolean autoY = true;
    private double minX = 0;
    private double maxX = 1;
    private double minY = 0;
    private double maxY = 1;

    private double windowX = Double.NaN;

    private String title = null;
    private Font fntTitle = new Font("Helvetica", Font.BOLD, 24);

    private double userTickX = Double.NaN;
    private double userTickY = Double.NaN;
    private Font fntLabel = new Font("Helvetica", Font.PLAIN, 12);

    private Color[] colors = null;

    private boolean refresh = true;

    static public final int CROSS = 0;
    static public final int LINE = 1;
    static private final int MODES = 2;
    private JRadioButtonMenuItem[] btnDrawMode = new JRadioButtonMenuItem[MODES];
    private JPopupMenu popupmenu = new JPopupMenu("");
    private JMenuItem[] userMenu = null;
    JMenu subSeries;
    private NumberFormat nf = NumberFormat.getNumberInstance();

    /**
     * Constructor. Create the panel with a default size of 200x200.
     */
    public PlotComponent() {
        super();

        nf.setMaximumFractionDigits(3);
        nf.setMinimumFractionDigits(0);

        ButtonGroup group = new ButtonGroup();
        btnDrawMode[CROSS] = new JRadioButtonMenuItem("Draw Cross", false);
        btnDrawMode[CROSS].addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                repaint();
            }
        });
        group.add(btnDrawMode[CROSS]);
        btnDrawMode[LINE] = new JRadioButtonMenuItem("Draw Line", true);
        btnDrawMode[LINE].addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                repaint();
            }
        });
        group.add(btnDrawMode[LINE]);

        contextMenu();
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popupmenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }

            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popupmenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        setBorder(new EmptyBorder(2, 2, 2, 2));
        setPreferredSize(new Dimension(400, 200));
    }

    // ------------------------------------------------------------------------

    public void setUserMenu(JMenuItem[] items) {
        userMenu = items;
        contextMenu();
    }

    private void contextMenu() {
        popupmenu.removeAll();

        JMenu file = new JMenu("File");
        popupmenu.add(file);
        file.add(new JMenuItem(new AbstractAction("Save Data") {
            public void actionPerformed(ActionEvent e) {
            	JFileChooser fc = new JFileChooser();
            	if(fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            		File fn = fc.getSelectedFile();
            		
					try {
						PrintWriter out;		
						out = new PrintWriter(new FileOutputStream(fn), true);
						
						int cnt = 0;
						for (Iterator iter = dataseries.iterator(); iter.hasNext();) {
							Series series = (Series) iter.next();
							out.println("");
							out.println("Series "+ (cnt++));
							for (Iterator iter2 = series.data.iterator(); iter2.hasNext();) {
								double[] v = (double[])iter2.next();
								out.println(v[0]+"\t"+v[1]);
							}
						}						
						
	            		out.flush();
	            		out.close();
	            		JOptionPane.showMessageDialog(null,"Data has been saved to "+fn.getName());
					} catch (FileNotFoundException e1) {
	            		JOptionPane.showMessageDialog(null, "Error: Cannot save File" +fn.getName()+ ".");
						e1.printStackTrace();
					}
            	}
            }
        }));
        
        JMenu sub = new JMenu("Options");
        popupmenu.add(sub);
        sub.add(btnDrawMode[CROSS]);
        sub.add(btnDrawMode[LINE]);

        sub.addSeparator();
        
        // TODO prettify
        sub.add(new JMenuItem(new AbstractAction("Min/Max") {
            public void actionPerformed(ActionEvent e) {
                double[] x = getRangeX();
                double[] y = getRangeY();
                Object val1, val2 = null;
                val1 = JOptionPane.showInputDialog("Min X", new Double(x[0]));
                if (val1 != null) {
                    val2 = JOptionPane.showInputDialog("Max X", new Double(x[1]));
                }
                if ((val1 != null) && (val2 != null)) {
                    try {
                        x[0] = Double.parseDouble(val1.toString());
                        x[1] = Double.parseDouble(val2.toString());
                        setAutoRangeX(false);
                        setRangeX(x[0], x[1]);
                    } catch(NumberFormatException exc) {
                        setAutoRangeX(true);
                    }
                } else {
                    setAutoRangeX(true);
                }

                val1 = JOptionPane.showInputDialog("Min Y", new Double(y[0]));
                if (val1 != null) {
                    val2 = JOptionPane.showInputDialog("Max Y", new Double(y[1]));
                }
                if ((val1 != null) && (val2 != null)) {
                    try {
                        y[0] = Double.parseDouble(val1.toString());
                        y[1] = Double.parseDouble(val2.toString());
                        setAutoRangeY(false);
                        setRangeY(y[0], y[1]);
                    } catch(NumberFormatException exc) {
                        setAutoRangeY(true);
                    }
                } else {
                    setAutoRangeY(true);
                }
            }
        }));

        
        sub.add(new JMenuItem(new AbstractAction("Auto Min/Max") {
            public void actionPerformed(ActionEvent e) {
                    setAutoRangeX(true);
                    setAutoRangeY(true);
            }
        }));
        
        JCheckBoxMenuItem chk = new JCheckBoxMenuItem("Auto Refresh", refresh);
        chk.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                refresh = ((JCheckBoxMenuItem)e.getSource()).isSelected();
            }
        });
        popupmenu.add(chk);

        popupmenu.addSeparator();

        subSeries = new JMenu("Show Series");
        popupmenu.add(subSeries);
        int[] pix = new int[64];
        synchronized (dataseries) {
            for (Iterator iter = dataseries.iterator(); iter.hasNext();) {
                Series series = (Series) iter.next();
                JMenuItem item = series.getMenuItem();
                pix[0] = getColor(series.getID()).getRGB();
                for (int i = 1; i < 64; i++) {
                    pix[i] = pix[0];
                }
                Image img = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(8, 8, pix, 0, 8));
                item.setIcon(new ImageIcon(img));
                subSeries.add(item);
                if ((subSeries.getItemCount() >= 20) && iter.hasNext()) {
                    JMenu more = new JMenu("More ...");
                    subSeries.insert(more, 0);
                    subSeries = more;
                }
            }
        }

        JMenuItem item = new JMenuItem(new AbstractAction("Show All") {
            public void actionPerformed(ActionEvent e) {
                showAllSeries();
            }
        });
        popupmenu.add(item);
        item = new JMenuItem(new AbstractAction("Hide All") {
            public void actionPerformed(ActionEvent e) {
                hideAllSeries();
            }
        });
        popupmenu.add(item);

        if (userMenu != null) {
            popupmenu.addSeparator();
            for (int i = 0; i < userMenu.length; i++) {
                popupmenu.add(userMenu[i]);
            }
        }
    }

    // ------------------------------------------------------------------------
    public boolean getAutRepaint() {
        return refresh;
    }

    public void setAutoRepaint(boolean repaint) {
        refresh = repaint;
        if (repaint) {
            repaint();
        }
    }

    // ------------------------------------------------------------------------
    public int getDrawMode() {
        for (int i = 0; i < MODES; i++) {
            if (btnDrawMode[i].isSelected()) {
                return i;
            }
        }
        return -1;
    }

    public void setDrawMode(int mode) {
        if ((mode >= 0) && (mode < MODES)) {
            btnDrawMode[mode].setSelected(true);
            repaint();
        }
    }

    // ------------------------------------------------------------------------

    /**
     *
     */
    public void reset() {
        synchronized (dataseries) {
            dataseries.clear();
            maxid = 0;
        }
        repaint();
    }

    public void resetData() {
        synchronized (dataseries) {
            for (Iterator iter = dataseries.iterator(); iter.hasNext();) {
                Series series = (Series) iter.next();
                series.resetData();
            }
        }
        repaint();
    }

    public void resetSeries(int id) {
        synchronized (dataseries) {
            for (Iterator iter = dataseries.iterator(); iter.hasNext();) {
                Series series = (Series) iter.next();
                if (series.getID() == id) {
                    series.resetData();
                }
            }
        }
        repaint();
    }

    // ------------------------------------------------------------------------

    /**
     * Returs the title of the plot.
     *
     * @return title of the plot.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the plot. This will set the title of the plot, which is
     * shown above the plot itself. Setting this to null will make no title be
     * shown.
     *
     * @param title the title of the plot.
     */
    public void setTitle(String title) {
        this.title = title;
        repaint();
    }

    /**
     * Returns the font used for drawing the title of the plot.
     *
     * @return font used for the title.
     */
    public Font getTitleFont() {
        return fntTitle;
    }

    /**
     * Sets the font used for drawing the tile above the plot. Setting this to
     * null will result in the title being drawn using the current font of the
     * graphics.
     *
     * @param font to be used when drawing the title.
     */
    public void setTitleFont(Font font) {
        fntTitle = font;
        repaint();
    }

    // ------------------------------------------------------------------------

    private Color getColor(int idx) {
        if (colors == null) {
            return Color.getHSBColor((float) idx / maxid, 1f, 0.9f);
        } else {
            if ((colors == null) || (idx < 0) || (idx >= colors.length)) {
                return Color.black;
            }
            return colors[idx];
        }
    }

    public Color[] getColors() {
        return colors;
    }

    public void setColors(Color[] colors) {
        this.colors = colors;
        repaint();
    }

    public ImageObject getAsImage() {
    	boolean opaqueValue = this.isOpaque();
		this.setOpaque( true );
		BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = image.createGraphics();
		this.paint(g2d);
		g2d.dispose();
		this.setOpaque( opaqueValue );
		
    	ImageObject im = null;
		try {
			im = ImageObject.getImageObject(image);
		} catch (ImageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return im;
    }
    
    
    public ImageObject getLegendAsImage() {
    	Component[] com = subSeries.getMenuComponents();
		JPanel legend = new JPanel(new GridLayout(com.length,2));
		JMenuItem item;
		for(int i=0; i<com.length; i++) {
			item = (JMenuItem)com[i];
			legend.add(new JLabel(item.getIcon()));		
			legend.add(new JLabel(item.getText()));
		}
		
		JFrame f = new JFrame();
		f.getContentPane().add(legend);
		f.pack();
	
	//	f.setVisible(true);
		BufferedImage image = new BufferedImage(legend.getWidth(), legend.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = image.createGraphics();
	
		legend.paint(g2d);
		
		g2d.dispose();
				
    	ImageObject im = null;
		try {
			im = ImageObject.getImageObject(image);
		} catch (ImageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return im;
    }
    
    // ------------------------------------------------------------------------

    public void setSeriesVisible(int id, boolean visible) {
        Series found = null;
        synchronized (dataseries) {
            for (Iterator iter = dataseries.iterator(); iter.hasNext();) {
                Series series = (Series) iter.next();
                if (series.getID() == id) {
                    found = series;
                    break;
                }
            }
            if (found == null) {
                throw(new IllegalArgumentException("No such series."));
            }
            found.setVisible(visible);
        }
        repaint();
    }

    public boolean isSeriesVisible(int id) {
        Series found = null;
        synchronized (dataseries) {
            for (Iterator iter = dataseries.iterator(); iter.hasNext();) {
                Series series = (Series) iter.next();
                if (series.getID() == id) {
                    found = series;
                    break;
                }
            }
            if (found == null) {
                throw(new IllegalArgumentException("No such series."));
            }
            return found.isVisible();
        }
    }

    public void showAllSeries() {
        synchronized (dataseries) {
            for (Iterator iter = dataseries.iterator(); iter.hasNext();) {
                Series series = (Series) iter.next();
                series.setVisible(true);
            }
        }
        repaint();
    }

    public void hideAllSeries() {
        synchronized (dataseries) {
            for (Iterator iter = dataseries.iterator(); iter.hasNext();) {
                Series series = (Series) iter.next();
                series.setVisible(false);
            }
        }
        repaint();
    }

    // ------------------------------------------------------------------------

    public void setWindowX(double windowx) {
        windowX = windowx;
        repaint();
    }

    public void setAutoRangeX(boolean auto) {
        autoX = auto;
        repaint();
    }

    public boolean isAutoRangeX() {
        return autoX;
    }

    public void setRangeX(double minX, double maxX) {
        this.minX = minX;
        this.maxX = maxX;
        repaint();
    }

    public double[] getRangeX() {
        double[] result = new double[]{0, 1};

        if (autoX) {
            boolean hit = false;
            synchronized (dataseries) {
                for (Iterator iter = dataseries.iterator(); iter.hasNext();) {
                    Series series = (Series) iter.next();
                    if (series.isVisible()) {
                        double[] tmp = series.getRangeX();
                        if (!hit) {
                            if (!Double.isNaN(tmp[0]) && !Double.isNaN(tmp[1])) {
                                result[0] = tmp[0];
                                result[1] = tmp[1];
                                hit = true;
                            }
                        } else {
                            if (tmp[0] < result[0]) result[0] = tmp[0];
                            if (tmp[1] > result[1]) result[1] = tmp[1];
                        }
                    }
                }
            }
            if (!hit) return new double[]{0, 1};
        } else {
            result[0] = minX;
            result[1] = maxX;
        }
        if (!Double.isNaN(windowX) && ((result[1] - result[0]) > windowX)) {
            result[0] = result[1] - windowX;
        }
        return result;
    }

    // ------------------------------------------------------------------------

    public void setAutoRangeY(boolean auto) {
        autoY = auto;
        repaint();
    }

    public boolean isAutoRangeY() {
        return autoY;
    }

    public void setRangeY(double minY, double maxY) {
        this.minY = minY;
        this.maxY = maxY;
        repaint();
    }

    public double[] getRangeY() {
        if (autoY) {
            boolean hit = false;
            double[] result = new double[]{0, 1};
            synchronized (dataseries) {
                for (Iterator iter = dataseries.iterator(); iter.hasNext();) {
                    Series series = (Series) iter.next();
                    if (series.isVisible()) {
                        double[] tmp = series.getRangeY();
                        if (!hit) {
                            if (!Double.isNaN(tmp[0]) && !Double.isNaN(tmp[1])) {
                                result[0] = tmp[0];
                                result[1] = tmp[1];
                                hit = true;
                            }
                        } else {
                            if (tmp[0] < result[0]) result[0] = tmp[0];
                            if (tmp[1] > result[1]) result[1] = tmp[1];
                        }
                    }
                }
            }
            return result;
        } else {
            return new double[]{minY, maxY};
        }
    }

    // ------------------------------------------------------------------------

    public void setValue(int id, Object v) throws IllegalArgumentException {
        Series found = null;
        synchronized (dataseries) {
            for (Iterator iter = dataseries.iterator(); iter.hasNext();) {
                Series series = (Series) iter.next();
                if (series.getID() == id) {
                    found = series;
                    break;
                }
            }
            if (found == null) {
                throw(new IllegalArgumentException("No such series."));
            }
            found.setValue(v);
        }
        if (refresh) {
            repaint();
        }
    }

    public void setValue(int id, Object x, Object v) throws IllegalArgumentException {
        Series found = null;
        synchronized (dataseries) {
            for (Iterator iter = dataseries.iterator(); iter.hasNext();) {
                Series series = (Series) iter.next();
                if (series.getID() == id) {
                    found = series;
                    break;
                }
            }
            if (found == null) {
                throw(new IllegalArgumentException("No such series."));
            }
            found.setValue(x, v);
        }
        if (refresh) {
            repaint();
        }
    }

    public void setValue(int id, double v) throws IllegalArgumentException {
        Series found = null;
        synchronized (dataseries) {
            for (Iterator iter = dataseries.iterator(); iter.hasNext();) {
                Series series = (Series) iter.next();
                if (series.getID() == id) {
                    found = series;
                    break;
                }
            }
            if (found == null) {
                throw(new IllegalArgumentException("No such series."));
            }
            found.setValue(v);
        }
        if (refresh) {
            repaint();
        }
    }

    public void setValue(int id, double x, double v) throws IllegalArgumentException {
        Series found = null;
        synchronized (dataseries) {
            for (Iterator iter = dataseries.iterator(); iter.hasNext();) {
                Series series = (Series) iter.next();
                if (series.getID() == id) {
                    found = series;
                    break;
                }
            }
            if (found == null) {
                throw(new IllegalArgumentException("No such series."));
            }
            found.setValue(x, v);
        }
        if (refresh) {
            repaint();
        }
    }

    // ------------------------------------------------------------------------

    public int addSeries(String legend) {
        int id = 0;
        synchronized (dataseries) {
            id = maxid;
            maxid++;
        }
        // has to be done outside of synchronized, otherwise deadlock with
        // paintcomponent.
        Series series = new Series(legend, id);
        synchronized (dataseries) {
            dataseries.add(series);
        }
        contextMenu();
        return id;
    }

    public void removeSeries(int id) {
        synchronized (dataseries) {
            for (Iterator iter = dataseries.iterator(); iter.hasNext();) {
                Series tmp = (Series) iter.next();
                if (tmp.getID() == id) {
                    iter.remove();
                    contextMenu();
                    return;
                }
            }
        }
    }

    // ------------------------------------------------------------------------

    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        double[] scale = new double[2];
        Rectangle area = new Rectangle();

        Font font = g.getFont();
        FontRenderContext frc = g2d.getFontRenderContext();
        Color color = g.getColor();

        Insets insets = getInsets();
        double[] rangeX = getRangeX();
        double[] rangeY = getRangeY();
        area.width = getWidth() - insets.left - insets.right;
        area.height = getHeight() - insets.top - insets.bottom;
        area.x = insets.left;
        area.y = insets.top;

        // draw the white background
        g.setColor(Color.white);
        g.fillRect(area.x, area.y, area.width, area.height);

        // draw title
        if (title != null) {
            Font fnt = (fntTitle == null) ? font : fntTitle;
            Rectangle2D rect = fnt.getStringBounds(title, frc);
            rect.getHeight();
            g.setColor(Color.black);
            g.setFont(fnt);
            g.drawString(title, area.x + (int) (area.width - rect.getWidth()) / 2, area.y - (int) rect.getY());
            area.height -= Math.ceil(rect.getHeight());
            area.y += Math.ceil(rect.getHeight());
        }

        // find the size of the min and max labels both x and y
        nf.setMinimumFractionDigits(nf.getMaximumFractionDigits());
        Font fnt = (fntLabel == null) ? font : fntLabel;
        String s = nf.format(rangeX[0]);
        Rectangle2D rect = fnt.getStringBounds(s, frc);
        double maxfontxw = rect.getWidth();
        double maxfontxh = rect.getHeight();
        s = nf.format(rangeX[1]);
        rect = fnt.getStringBounds(s, frc);
        if (rect.getWidth() > maxfontxw) maxfontxw = rect.getWidth();
        if (rect.getHeight() > maxfontxh) maxfontxh = rect.getHeight();

        s = nf.format(rangeY[0]);
        rect = fnt.getStringBounds(s, frc);
        double maxfontyh = rect.getHeight();
        double maxfontyw = rect.getWidth();
        s = nf.format(rangeY[1]);
        rect = fnt.getStringBounds(s, frc);
        if (rect.getWidth() > maxfontyw) maxfontyw = rect.getWidth();
        if (rect.getHeight() > maxfontyh) maxfontyh = rect.getHeight();

        nf.setMinimumFractionDigits(0);

        // subtract the label height and width from drawing space.
        area.y = (int) Math.ceil(area.y + 2);
        area.height = (int) Math.ceil(area.height - 2 - maxfontxh);
        area.x = (int) Math.ceil(area.x + maxfontyw);
        area.width = (int) Math.ceil(area.width - maxfontyw - 2);

        // draw min/max labels
        g.setFont(fnt);
        s = nf.format(rangeX[0]);
        rect = fnt.getStringBounds(s, frc);
        g.setColor(Color.black);
        g.drawString(s, (int) Math.floor(area.x),
                     (int) Math.ceil(area.y + area.height - rect.getY()));
        g.setColor(Color.lightGray);
        g.drawLine(area.x, area.y, area.x, area.y + area.height);

        s = nf.format(rangeX[1]);
        rect = fnt.getStringBounds(s, frc);
        g.setColor(Color.black);
        g.drawString(s, (int) Math.floor(area.x + area.width - rect.getWidth()),
                     (int) Math.ceil(area.y + area.height - rect.getY()));
        g.setColor(Color.lightGray);
        g.drawLine(area.x + area.width, area.y, area.x + area.width, area.y + area.height);

        s = nf.format(rangeY[0]);
        rect = fnt.getStringBounds(s, frc);
        g.setColor(Color.black);
        g.drawString(s, (int) Math.floor(insets.left + maxfontyw - rect.getWidth()),
                     (int) Math.floor(area.y + area.height));
        g.setColor(Color.lightGray);
        g.drawLine(area.x, area.y, area.x + area.width, area.y);

        s = nf.format(rangeY[1]);
        rect = fnt.getStringBounds(s, frc);
        g.setColor(Color.black);
        g.drawString(s, (int) Math.floor(insets.left + maxfontyw - rect.getWidth()),
                     (int) Math.floor(area.y - rect.getY()));
        g.setColor(Color.lightGray);
        g.drawLine(area.x, area.y + area.height, area.x + area.width, area.y + area.height);

        s = nf.format(0);
        rect = fnt.getStringBounds(s, frc);
        g.setColor(Color.black);

        // calculate the scale.
        scale[0] = area.width / (rangeX[1] - rangeX[0]);
        scale[1] = area.height / (rangeY[1] - rangeY[0]);
        if (scale[0] == Double.POSITIVE_INFINITY) scale[0] = 1;
        if (scale[1] == Double.POSITIVE_INFINITY) scale[1] = 1;

        // calculate the location of the zero line and draw it
        int zeroy, zerox;
        s = nf.format(0);
        rect = fnt.getStringBounds(s, frc);
        g.setColor(Color.black);
        if ((rangeX[0] >= 0) || (rangeX[1] <= 0)) {
            zerox = area.x;
        } else {
            zerox = area.x - (int) (rangeX[0] * scale[0]);
        }
        g.drawLine(zerox, area.y, zerox, area.y + area.height);
        if ((rangeY[0] >= 0) || (rangeY[1] <= 0)) {
            zeroy = area.y + area.height;
        } else {
            zeroy = (int) (area.y + rangeY[1] * scale[1]);
        }
        g.drawLine(area.x, zeroy, area.x + area.width, zeroy);

        // draw Y gridlines
        double dist, step, d, l;
        dist = maxfontyh / scale[1];
        if (!Double.isNaN(userTickY)) {
            step = userTickY;
        } else {
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
        }

        d = step * Math.ceil((rangeY[0] + dist * 1.5) / step);
        while (d <= rangeY[1] - dist * 1.5) {
            s = nf.format(d);
            rect = fnt.getStringBounds(s, frc);
            g.setColor(Color.black);
            g.drawString(s, (int) Math.floor(insets.left + maxfontyw - rect.getWidth()),
                         (int) Math.floor(area.y + area.height - (d - rangeY[0]) * scale[1] - rect.getY() / 2));
            if (d != 0.0) {
                g.setColor(Color.lightGray);
            }
            g.drawLine(area.x, (int) Math.floor(area.y + area.height - (d - rangeY[0]) * scale[1]),
                       area.x + area.width, (int) Math.floor(area.y + area.height - (d - rangeY[0]) * scale[1]));
            d += step;
        }

        // draw the gridlines on the X-axis
        dist = maxfontxw / scale[0];
        if (!Double.isNaN(userTickX)) {
            step = userTickX;
        } else {
            step = 3 * dist;
            l = Math.ceil(Math.log(step) / Math.log(10)) - 1;
            l = Math.pow(10, -l);
            step = step * l;
            if (step <= 2.5) {
                step = 2.5 / l;
            } else if (step <= 5.0) {
                step = 5 / l;
            } else {
                step = 10 / l;
            }
        }

        d = step * Math.ceil((rangeX[0] + dist * 1.5) / step);
        while (d <= rangeX[1] - dist * 1.5) {
            s = nf.format(d);
            rect = fnt.getStringBounds(s, frc);
            g.setColor(Color.black);
            g.drawString(s, (int) Math.floor(area.x + (d - rangeX[0]) * scale[0] - rect.getWidth() / 2),
                         (int) Math.ceil(area.y + area.height - rect.getY()));
            if (d != 0.0) {
                g.setColor(Color.lightGray);
            }
            g.drawLine((int) Math.floor(area.x + (d - rangeX[0]) * scale[0]), area.y,
                       (int) Math.floor(area.x + (d - rangeX[0]) * scale[0]), area.y + area.height);
            d += step;
        }

        // calculate offset to be used when drawing, and set the cliparea.
        g.setClip(area.x, area.y, area.width, area.height);

        // draw data
        double offx = Math.floor(scale[0] * rangeX[0]) - area.x;
        double offy = Math.floor(scale[1] * rangeY[0]) + area.height + area.y;
        synchronized (dataseries) {
            for (Iterator iter = dataseries.iterator(); iter.hasNext();) {
                Series series = (Series) iter.next();
                if (series.isVisible()) {
                    g.setColor(getColor(series.getID()));
                    series.paint(g, scale[0], scale[1], offx, offy);
                }
            }
        }

        g.setClip(null);
        g.setFont(font);
        g.setColor(color);
    }

    class Series {
        private String legend;
        private int id;
        private double autoMinX = Double.NaN;
        private double autoMaxX = Double.NaN;
        private double autoMinY = Double.NaN;
        private double autoMaxY = Double.NaN;
        private ArrayList data = new ArrayList();

        private JCheckBoxMenuItem menu;

        public Series(String legend, int id) {
            this.legend = (legend == null) ? "Series " + id : legend;
            this.id = id;
            menu = new JCheckBoxMenuItem(this.legend, true);
            menu.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    repaint();
                }
            });
        }

        public int getID() {
            return id;
        }

        public boolean isVisible() {
            return menu.isSelected();
        }

        public void setVisible(boolean vis) {
            menu.setSelected(vis);
        }

        public JMenuItem getMenuItem() {
            return menu;
        }

        public void setValue(Object v) throws IllegalArgumentException {
            if (!v.getClass().isArray()) {
                throw(new IllegalArgumentException("v need to be an array."));
            }
            int len = Array.getLength(v);
            for (int i = 0; i < len; i++) {
                setValue(i, Double.parseDouble(Array.get(v, i).toString()));
            }
        }

        public void setValue(Object x, Object v) throws IllegalArgumentException {
            if (!x.getClass().isArray() || !v.getClass().isArray()) {
                throw(new IllegalArgumentException("x and v need to be an array."));
            }
            if (Array.getLength(x) != Array.getLength(v)) {
                throw(new IllegalArgumentException("Need same number of x and v values."));
            }
            int len = Array.getLength(x);
            for (int i = 0; i < len; i++) {
                setValue(Array.getDouble(x, i), Array.getDouble(v, i));
            }
        }

        public void setValue(double v) throws IllegalArgumentException {
            setValue(data.size(), v);
        }

        public void setValue(double x, double v) {
            if (Double.isNaN(autoMinX) || (x < autoMinX)) autoMinX = x;
            if (Double.isNaN(autoMaxX) || (x > autoMaxX)) autoMaxX = x;
            if (Double.isNaN(autoMinY) || (v < autoMinY)) autoMinY = v;
            if (Double.isNaN(autoMaxY) || (v > autoMaxY)) autoMaxY = v;

            double[] newval = new double[]{x, v};
            for (int i = 0; i < data.size(); i++) {
                double tmp[] = (double[]) data.get(i);
                if (tmp[0] == x) {
                    data.set(i, newval);
                    return;
                } else if (tmp[0] > x) {
                    data.add(i, newval);
                    return;
                }
            }
            data.add(newval);
        }

        public void resetData() {
            autoMinX = Double.NaN;
            autoMaxX = Double.NaN;
            autoMinY = Double.NaN;
            autoMaxY = Double.NaN;

            data.clear();
        }

        public double[] getRangeX() {
            return new double[]{autoMinX, autoMaxX};
        }

        public double[] getRangeY() {
            return new double[]{autoMinY, autoMaxY};
        }

        public void paint(Graphics g, double sx, double sy, double offx, double offy) {
            double[] pt0, pt1;
            synchronized (data) {
                if (btnDrawMode[LINE].isSelected()) {
                    for (int i = 0; i < data.size() - 1; i++) {
                        pt0 = (double[]) data.get(i);
                        pt1 = (double[]) data.get(i + 1);
                        g.drawLine((int) (sx * pt0[0] - offx), (int) (offy - sy * pt0[1]),
                                   (int) (sx * pt1[0] - offx), (int) (offy - sy * pt1[1]));
                    }
                } else if (btnDrawMode[CROSS].isSelected()) {
                    for (int i = 0; i < data.size(); i++) {
                        pt0 = (double[]) data.get(i);
                        g.drawLine((int) (sx * pt0[0] - offx - 2), (int) (offy - sy * pt0[1]) - 2,
                                   (int) (sx * pt0[0] - offx + 2), (int) (offy - sy * pt0[1]) + 2);
                        g.drawLine((int) (sx * pt0[0] - offx + 2), (int) (offy - sy * pt0[1]) - 2,
                                   (int) (sx * pt0[0] - offx - 2), (int) (offy - sy * pt0[1]) + 2);
                    }
                }
            }
        }

        public String toString() {
            return legend;
        }
    }
}
