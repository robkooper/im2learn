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
package edu.illinois.ncsa.isda.im2learn.ext.info;


import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.table.TableCellRenderer;

import edu.illinois.ncsa.isda.im2learn.core.display.Im2LearnMenu;
import edu.illinois.ncsa.isda.im2learn.core.display.ImagePanel;
import edu.illinois.ncsa.isda.im2learn.core.display.ImageUpdateEvent;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Show the memory usage and environment variables.
 */
public class DebugDialog extends JFrame implements Im2LearnMenu {
    private MemoryGraph memory = new MemoryGraph();
    private long memorysleep = 100;

    public DebugDialog() {
        super("DEBUG Dialog");

        JTabbedPane tabpane = new JTabbedPane();
        getContentPane().add(tabpane, BorderLayout.CENTER);

        // memory
        JPanel panel = new JPanel(new BorderLayout());
        tabpane.add("Memory", panel);

        panel.add(memory, BorderLayout.CENTER);

        JPanel tmp = new JPanel();
        JButton btn = new JButton(new AbstractAction("Force GC") {
            public void actionPerformed(ActionEvent e) {
                System.gc();
            }
        });
        tmp.add(btn);

        JLabel lbl = new JLabel("ms between updates");
        tmp.add(lbl);
        JComboBox cmb = new JComboBox(new String[]{"10", "100", "200", "500", "1000"});
        cmb.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox cmb = (JComboBox) e.getSource();
                memorysleep = Long.parseLong(cmb.getSelectedItem().toString());
            }
        });
        cmb.setSelectedIndex(2);
        tmp.add(cmb);

        panel.add(tmp, BorderLayout.SOUTH);

        // environment
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = 1;
        panel = new JPanel(gbl);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0.0;
        gbc.gridy = 0;

        Properties prop = System.getProperties();
        String[][] rows = new String[prop.size()][2];
        int i = 0;
        for(Enumeration e=prop.keys(); e.hasMoreElements(); i++) {
            Object key = e.nextElement();
            String val = prop.get(key).toString();
            val = val.replaceAll("\r", "\\\\r");
            val = val.replaceAll("\n", "\\\\n");

            rows[i][0] = key.toString();
            rows[i][1] = val;
        }

        JTable table = new JTable(rows, new String[]{"Property Name", "Property Value"});
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        TableModel model = table.getModel();
        Component comp = null;
        int cellWidth = 0;

        // find a good starting size.
        int max0 = Integer.MIN_VALUE;
        int max1 = Integer.MIN_VALUE;
        TableCellRenderer tcr0 = table.getDefaultRenderer(model.getColumnClass(0));
        TableCellRenderer tcr1 = table.getDefaultRenderer(model.getColumnClass(1));
        for(int r=0; r<rows.length; r++) {
            comp = tcr0.getTableCellRendererComponent(table, rows[r][0], false, false, r, 0);
            cellWidth = comp.getPreferredSize().width;
            if (cellWidth > max0) max0 = cellWidth;

            comp = tcr1.getTableCellRendererComponent(table, rows[r][1], false, false, r, 1);
            cellWidth = comp.getPreferredSize().width;
            if (cellWidth > max1) max1 = cellWidth;
        }

        table.getColumnModel().getColumn(0).setPreferredWidth(max0);
        table.getColumnModel().getColumn(1).setPreferredWidth(max1);

        JScrollPane pane = new JScrollPane(table);
        pane.setPreferredSize(memory.getPreferredSize());
        tabpane.add("Environment", pane);

        pack();

        // Catch window show/hide
        addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent e) {
                memory.startthread();
            }

            public void componentHidden(ComponentEvent e) {
                memory.stopthread();
            }
        });
    }

    /**
     * ignored
     *
     * @param imagepanel
     */
    public void setImagePanel(ImagePanel imagepanel) {
    }

    /**
     * No panel menu.
     *
     * @return null
     */
    public JMenuItem[] getPanelMenuItems() {
        return null;
    }

    /**
     * This dialog is added to the Vis menu.
     *
     * @return vis menu and menuitem for this dialog.
     */
    public JMenuItem[] getMainMenuItems() {
        JMenu vis = new JMenu("Help");
        JMenuItem menu = new JMenuItem("Debug");
        menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pack();
                setVisible(true);
            }
        });
        vis.add(menu);
        return new JMenuItem[]{vis};
    }

    /**
     * ignored
     *
     * @param event
     */
    public void imageUpdated(ImageUpdateEvent event) {
    }

    class MemoryGraph extends JPanel implements Runnable {
        private int maxpoints = 400;
        private long maxval = 0;
        private BufferedImage bi = new BufferedImage(maxpoints, 100, BufferedImage.TYPE_BYTE_INDEXED);
        private long[][] points = new long[3][maxpoints + 1];
        private Runtime rt = Runtime.getRuntime();
        private NumberFormat nf = NumberFormat.getIntegerInstance();
        private Thread memthread = null;

        public MemoryGraph() {
            setOpaque(true);
            setBackground(Color.black);
            setFont(new Font("Dialog", Font.PLAIN, 10));
            setPreferredSize(new Dimension(maxpoints, 100));
        }

        public void startthread() {
            if (memthread != null) {
                return;
            }

            memthread = new Thread(this);
            memthread.start();
            memthread.setName("Im2Learn - Debug memory graph");
        }

        public void stopthread() {
            if (memthread == null) {
                return;
            }

            Thread tmp = memthread;
            memthread = null;
            tmp.interrupt();
            try {
                tmp.join();
            } catch (InterruptedException exc) {
            }
        }

        public void run() {
            while (memthread == Thread.currentThread()) {
                // store points
                points[0][maxpoints] = rt.freeMemory();
                points[1][maxpoints] = rt.totalMemory();
                points[2][maxpoints] = rt.maxMemory();

                // move all points
                maxval = 0;
                for (int i = 1; i <= maxpoints; i++) {
                    if (maxval < points[0][i]) maxval = points[0][i];
                    if (maxval < points[1][i]) maxval = points[1][i];
                    if (maxval < points[2][i]) maxval = points[2][i];

                    points[0][i - 1] = points[0][i];
                    points[1][i - 1] = points[1][i];
                    points[2][i - 1] = points[2][i];
                }

                drawGraph();
                repaint();

                // wait
                try {
                    Thread.sleep(memorysleep);
                } catch (InterruptedException exc) {
                }
            }
        }

        private void drawGraph() {
            double x1, x2, sx, sy;
            int i;
            int w = getWidth();
            int h = getHeight();

            if ((bi.getWidth() != w) || (bi.getHeight() != h)) {
                bi = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_INDEXED);
            }
            Graphics g = bi.getGraphics();

            sx = (double) w / maxpoints;
            sy = (double) h / maxval;

            g.setColor(getBackground());
            g.setFont(getFont());
            g.fillRect(0, 0, w, h);

            g.setColor(Color.green);
            g.drawString("Used", 2, 10);
            g.drawString(nf.format(points[1][maxpoints] - points[0][maxpoints]), 30, 10);

            g.setColor(Color.yellow);
            g.drawString("Total", 2, 20);
            g.drawString(nf.format(points[1][maxpoints]), 30, 20);

            g.setColor(Color.cyan);
            g.drawString("Max", 2, 30);
            g.drawString(nf.format(points[2][maxpoints]), 30, 30);

            g.setColor(Color.white);
            g.drawString("Free", 2, 40);
            g.drawString(nf.format(points[0][maxpoints]), 30, 40);

            // draw points
            for (i = 0, x1 = 0, x2 = sx; i < maxpoints - 1; i++, x1 += sx, x2 += sx) {
                g.setColor(Color.green);
                long used0 = points[1][i] - points[0][i];
                long used1 = points[1][i+1] - points[0][i+1];
                g.drawLine((int) x1, h - (int) (used0 * sy), (int) x2, h - (int) (used1 * sy));

                g.setColor(Color.yellow);
                g.drawLine((int) x1, h - (int) (points[1][i] * sy), (int) x2, h - (int) (points[1][i + 1] * sy));

                g.setColor(Color.cyan);
                g.drawLine((int) x1, h - (int) (points[2][i] * sy), (int) x2, h - (int) (points[2][i + 1] * sy));
            }
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(bi, 0, 0, null);
        }
    }

    public URL getHelp(String topic) {
        // TODO Auto-generated method stub
        return null;
    }
}


