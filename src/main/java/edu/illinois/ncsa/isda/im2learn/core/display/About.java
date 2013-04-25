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
 *   Neither the names of University of Illinois/NCSA, nor the names of
 *   its contributors may be used to endorse or promote products derived
 *   from this Software without specific prior written permission.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 * 
 *******************************************************************************/
package edu.illinois.ncsa.isda.imagetools.core.display;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.Im2LearnUtilities;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Properties;
import java.util.Vector;

/**
 * Show Im2Learn splash screen.
 */
public class About extends Im2LearnFrame implements Runnable {
    /**
     * Location of im2learn logo inside jar file
     */
    static private String icon = "im2learn.gif";

    /**
     * Label with logo, so only need to load it once.
     */
    static private JLabel im2learnabout = null;

    /**
     * Bold font used, so only need to load it once.
     */
    static private Font boldfont = null;

    /**
     * Normal font used, so only need to load it once.
     */
    static private Font normfont = null;

    /**
     * Thread that is responsible for the scrolling.
     */
    private Thread scrollthread = null;

    /**
     * Class that makes text scroll in about box.
     */
    private TextScroll textscroll = null;

    private static Log logger = LogFactory.getLog(About.class);

    private static About instance;

	private static Properties props;
    
    static {
    	props = new Properties();
		props.put("build", "unknown");

		// load specifics
		try {
			props.load(About.class.getResourceAsStream("about.properties"));
		} catch (Throwable exc) {
		}

		props.put("application", "Im2Learn");
		props.put("version", "2.1");
		props.put("copyright", "Copyright (c) 2000-2005 NCSA");
		props.put("homepage", "http://isda.ncsa.uiuc.edu/");
		props.put("company", "Image Spatial Data Analysis Group\\" +
						     "Cyber-infrastructure Division\\" +
						     "National Center for Supercomputing Applications\\" +
						     "University of Illinois, Urbana Champaign");
		props.put("authors", "Peter Bajcsy\\" +
							 "Rob Kooper\\" +
							 "Young-Jin Lee\\" +
							 "David Clutter\\" +
							 "Luigi Marini\\" +
							 "Tyler J. Alumbaugh\\" +
							 "Yi-Ting Chou\\" +
							 "Wei-Wen Feng\\" +
							 "Peter Ferak\\" +
							 "Peter Groves\\" +
							 "Ryo Kondo\\" +
							 "Sang-Chul Lee\\" +
							 "Tenzing Shaw\\" +
							 "Henrik Lomotan\\" +
							 "James Rapp\\" +
							 "Sunayana Saha\\" +
							 "David Scherba\\" +
							 "Martin Urban");
    }
    
    /**
     * This will return the name of the application.
     * 
     * @return the name of the application.
     */
    static public String getApplication() {
    	return (String)props.get("application");
    }

    /**
     * This will return the version of the application as major.minor
     * 
     * @return the version number
     */
    static public String getVersion() {
    	return (String)props.get("version");
    }

    /**
     * Returns the build number of the application. This is updated when
     * the build script is executed.
     * 
     * @return the build number
     */
    static public String getBuild() {
    	return (String)props.get("build");
    }

    /**
     * The copyright message for the application.
     * 
     * @return copyright message
     */
    static public String getCopyright() {
    	return (String)props.get("copyright");
    }

    /**
     * Returns the URL of the homepage of the group.
     * 
     * @return homepage URL.
     */
    static public String getHomePage() {
    	return (String)props.get("homepage");
    }

    /**
     * Returns a list of the groups/comapanies that have the copyright.
     * 
     * @return list of groups/companies
     */
    static public String[] getCompany() {
    	return ((String)props.get("company")).split("\\\\");
    }

    /**
     * Returns a list of all authors involved with this software.
     * 
     * @return list of authors
     */
    static public String[] getAuthors() {
    	return ((String)props.get("authors")).split("\\\\");
    }

    static public void showAbout() {
        showAbout(null);
    }

     static public void showAbout(Window win) {
        About about = getInstance();
        if (Im2LearnUtilities.isMACOS()) {
            about.setLocationRelativeTo(null);
        } else {
            about.setLocationRelativeTo(win);
        }
        about.setVisible(true);
    }

    static public void hideAbout() {
        getInstance().setVisible(false);
    }

    static public boolean isAboutVisible() {
        return getInstance().isVisible();
    }

     static private About getInstance() {
        if (instance == null) {
            instance = new About();
        }
        return instance;
    }
     
    /**
     * Create the about screen ready for display showing the version, the build
     * and the copyright holders.
     */
    protected About() {
        super("About im2learn");
        setResizable(false);

        // load some globals
        if (boldfont == null) {
            boldfont = new Font("Helvetica", Font.BOLD, 14);
            if (boldfont == null) {
                boldfont = new Font("Ariel", Font.BOLD, 14);
            }
        }

        if (normfont == null) {
            normfont = new Font("Helvetica", Font.PLAIN, 12);
            if (normfont == null) {
                normfont = new Font("Ariel", Font.PLAIN, 12);
            }
        }

        if (im2learnabout == null) {
            ImageIcon img = new ImageIcon(About.class.getResource(icon));
            im2learnabout = new JLabel(img);
            im2learnabout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            im2learnabout.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    try {
                        BrowserLauncher.openURL(getHomePage());
                    } catch (IOException exc) {
                        logger.error("Opening URL", exc);
                    }
                }
            });
        }

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));

        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));

        box.add(getText(getApplication() + "   " + getVersion(), boldfont));
        box.add(getText("build: " + getBuild(), normfont));
        JTextField tf = getText(getHomePage(), normfont);
        tf.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        tf.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                try {
                    BrowserLauncher.openURL(getHomePage());
                } catch (IOException exc) {
                    logger.error("Opening URL", exc);
                }
            }
        });
        box.add(tf);

        textscroll = new TextScroll();
        textscroll.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lbl;
        for (int i = 0; i < getCompany().length; i++) {
            lbl = new JLabel(getCompany()[i]);
            lbl.setFont(normfont);
            lbl.setForeground(Color.BLACK);
            textscroll.addText(lbl);
        }

        lbl = new JLabel("");
        lbl.setFont(normfont);
        lbl.setForeground(Color.BLACK);
        textscroll.addText(lbl);

        lbl = new JLabel("Authors:");
        lbl.setFont(boldfont);
        lbl.setForeground(Color.BLACK);
        textscroll.addText(lbl);
        for (int i = 0; i < getAuthors().length; i++) {
            lbl = new JLabel(getAuthors()[i]);
            lbl.setFont(normfont);
            lbl.setForeground(Color.BLACK);
            textscroll.addText(lbl);
        }

        box.add(textscroll);

        // copyright
        box.add(getText(getCopyright(), normfont));

        // add the box to the panel
        panel.add(box, BorderLayout.CENTER);

        // add the logo to the panel
        JPanel tmp = new JPanel(new BorderLayout());
        tmp.setOpaque(false);
        tmp.add(im2learnabout, BorderLayout.CENTER);

        // add close button
        tmp.add(new JButton(new AbstractAction("Close") {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        }), BorderLayout.SOUTH);

        panel.add(tmp, BorderLayout.EAST);

        getContentPane().add(panel);
        pack();
    }

    public void showing() {
        start();
    }

    public void closing() {
    	stop();
    }

    /**
     * Create a textfield with no border etc, basicly a lable which you can
     * select the text from.
     *
     * @param txt  to be added
     * @param font of the lalel
     * @return textfield looking like a label.
     */
    private JTextField getText(String txt, Font font) {
        JTextField tf = new JTextField(txt);
        tf.setBorder(null);
        tf.setOpaque(false);
        tf.setEditable(false);
        tf.setFont(font);
        tf.setHorizontalAlignment(JTextField.CENTER);
        return tf;
    }

    /**
     * start the scrolling window
     */
    private void start() {
        scrollthread = new Thread(this);
        scrollthread.start();
        scrollthread.setName("im2learn - About");
    }

    /**
     * stop the scrolling window
     */
    private void stop() {
        if (scrollthread != null) {
            Thread tmp = scrollthread;
            scrollthread = null;
            tmp.interrupt();
            try {
                tmp.join();
            } catch (InterruptedException e) {
            }
        }
        textscroll.reset();
    }

    /**
     * repaint the window resulting in a scroll.
     */
    public void run() {
        while (scrollthread == Thread.currentThread()) {
            repaint();
            try {
                Thread.sleep(50);
            } catch (InterruptedException exc) {
            }
        }
    }

    /**
     * A component that will scroll through the text fields
     */
    class TextScroll extends JComponent {
        private Vector text;
        private int yloc = 0;
        private Dimension pref;

        /**
         * Create the scrolling text with no text.
         */
        public TextScroll() {
            this.text = new Vector();
            this.pref = new Dimension(100, 100);
        }

        /**
         * Start scrolling from the beginning.
         */
        public void reset() {
            yloc = 0;
        }

        /**
         * Add text to the list of text to scroll. The label is used to specify
         * font and color. This will also calculate the preferred width.
         *
         * @param text to be added to scrolling text.
         */
        public void addText(JLabel text) {
            if (text == null) return;

            FontRenderContext frc = new FontRenderContext(null, true, true);
            Rectangle2D rect = text.getFont().getStringBounds(text.getText(), frc);
            if (rect.getWidth() > pref.width) pref.width = (int) rect.getWidth() + 11;
            this.text.add(text);
        }

        /**
         * Paint the scrolling text. If text has all been shown, start at the
         * bottom.
         *
         * @param g graphics to paint on.
         */
        public void paint(Graphics g) {
            if (text.size() == 0) {
                return;
            }

            FontRenderContext frc = ((Graphics2D) g).getFontRenderContext();

            g.setClip(0, 5, getWidth(), getHeight() - 5);
            int y = getHeight() - yloc;
            for (int i = 0; i < text.size(); i++) {
                JLabel lbl = (JLabel) text.get(i);
                Font font = lbl.getFont();
                String txt = lbl.getText();
                g.setFont(font);
                g.setColor(lbl.getForeground());
                Rectangle2D r = font.getStringBounds(txt, frc);
                int x = (int) (getWidth() - r.getWidth()) / 2;
                g.drawString(txt, x, y);
                y += (int) r.getHeight() + 1;
            }
            if (y < 0) {
                yloc = 0;
            } else {
                yloc++;
            }
        }

        /**
         * Return the calculated height and width.
         *
         * @return prefered size of component.
         */
        public Dimension getPreferredSize() {
            return pref;
        }
    }
}
