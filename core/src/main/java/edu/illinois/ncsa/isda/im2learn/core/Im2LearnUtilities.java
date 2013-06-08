package edu.illinois.ncsa.isda.im2learn.core;


import javax.swing.*;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageException;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObjectByte;
import edu.illinois.ncsa.isda.im2learn.core.display.About;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Random;

/**
 * Set of utilities that are used in Im2Learn but are not specific to a single
 * class.
 *
 * @author Rob Kooper
 * @version 2.0
 */
public class Im2LearnUtilities {
    static final public int WINDOWS = 0;
    static final public int MAC = 1;
    static final public int UNKNOWN = 2;
    static private int os = UNKNOWN;

    /**
     * Check what OS is running and remember this. If running on a Mac then set
     * a property such that the menubar will appear at the top of the screen.
     */
    static {
        // find out the os running
        String vers = System.getProperty("os.name").toLowerCase();
        if (vers.indexOf("windows") != -1) {
            os = WINDOWS;
        } else if (vers.indexOf("mac") != -1) {
            os = MAC;
        }
    }

    /**
     * Checks to see if this is a Mac. Some UI changes are needed to make this
     * look more like a real Mac Application. This function will return true if
     * the application is running on a Mac.
     *
     * @return true if running on a Mac, false otherwise.
     */
    static public boolean isMACOS() {
        return os == MAC;
    }

    /**
     * Return the OS the application is running on. To see if this is a Mac use
     * isMACOS.
     *
     * @return which OS the application is running on.
     */
    static public int getOS() {
        return os;
    }
   
    /**
     * Returns true if the application is running as a webservice.
     * 
     * @return true if this is running as a webservice.
     */
    static public boolean isWebService() {
        return System.getProperty("javawebstart.version") != null;
    }

    /**
     * There are 2 different file dialogs. One based on Swing and one on AWT.
     * The AWT interface is suggested on the Mac, for windows the swing
     * interface is suggested. The user can select which dialog is show by
     * setting the system property filedialog to either swing or awt. This can
     * be done at runtime or by adding -Dfiledialog=awt to the command line.
     *
     * @return true if the AWT dialog should be used.
     */
    static public boolean useAWTFileDialog() {
        String dialog = System.getProperty("filedialog");
        if ((dialog != null) && dialog.equalsIgnoreCase("awt")) {
            return true;
        }
        if ((dialog != null) && dialog.equalsIgnoreCase("swing")) {
            return false;
        }
        return isMACOS();
    }

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
    static public ImageObject createEmptyImage() {
        String msg;
        Rectangle2D rec;
        int x, y, i, rgb;
        int j = 0;
        String[] authors = About.getAuthors();
        String[] company = About.getCompany();
        Rectangle2D[] taken = new Rectangle2D[authors.length + company.length + 2];

        BufferedImage image = new BufferedImage(640, 480, BufferedImage.TYPE_INT_RGB);

        Graphics2D g = image.createGraphics();
        FontRenderContext frc = g.getFontRenderContext();

        // find a good font to use
        Font font = new Font("Helvetica", Font.BOLD, 42);
        if (font == null) {
            font = new Font("Arial", Font.BOLD, 42);
        }
        if (font == null) {
            font = new Font("Sans Serif", Font.BOLD, 42);
        }
        
        // fill background white
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 640, 480);

        // draw header in red.
        msg = About.getApplication();
        g.setColor(Color.RED);
        g.setFont(font);
        rec = font.getStringBounds(msg, frc);
        taken[j++] = new Rectangle2D.Double(320 - rec.getCenterX(), 100,
    			rec.getWidth(), rec.getHeight());
        g.drawString(msg, (int) (320 - rec.getCenterX()), 100);

        // list of company names
        g.setColor(Color.BLUE);
        font = font.deriveFont(24.0f);
        g.setFont(font);
        y = 200;
        for (i = 0; i < company.length; i++, y += 30) {
            msg = company[i];
            rec = font.getStringBounds(msg, frc);
            taken[j++] = new Rectangle2D.Double(320 - rec.getCenterX(), y,
					  rec.getWidth(), rec.getHeight());
            g.drawString(msg, (int) (320 - rec.getCenterX()), y);
        }

        // copyright message
        msg = About.getCopyright();
        g.setColor(Color.BLACK);
        font = font.deriveFont(14.0f);
        g.setFont(font);
        rec = font.getStringBounds(msg, frc);
        taken[j++] = new Rectangle2D.Double(320 - rec.getCenterX(), 450,
				  rec.getWidth(), rec.getHeight());
        g.drawString(msg, (int) (320 - rec.getCenterX()), 450);

        // add authors to background
        g.setColor(Color.LIGHT_GRAY);
        font = font.deriveFont(16.0f);
        g.setFont(font);
        Random rand = new Random();
        for(i=0; i<About.getAuthors().length; i++) {
        	msg = About.getAuthors()[i];
            rec = font.getStringBounds(msg, frc);
            boolean notok = true;
            do {
            	x = rand.nextInt(640 - (int)rec.getWidth());
            	y = (int)rec.getHeight() + rand.nextInt(480 - (int)rec.getHeight());
                taken[j] = new Rectangle2D.Double(x, y, rec.getWidth(), rec.getHeight());
            	notok = false;
            	for(int k=0; k<j && !notok; k++) {
            		notok = taken[k].intersects(taken[j]);
            	}
            } while(notok);
            j++;
            g.drawString(msg, x, y);        	
        }

        // convert image to ImageObject
        try {
        	return ImageObject.getImageObject(image);
        } catch (ImageException exc) {
        	return null;
        }
    }

    /**
     * Adds the menu item to the menu. If the menu item is a JMenu it will try
     * and find an existing copy of the menu, by name, and reuse it. If not
     * found it will add the JMenu to the menubar. If the menuitem is not a
     * JMenu it will add it to the appropriate menu if parent is a menu, or to
     * the EXT menu if parent is the menubar.
     *
     * @param parent the menubar or menu to which to attach the menuitem.
     * @param item   the menuitem to attach to the parent menu.
     */
    static public void addSubMenu(Object parent, JMenuItem item) {
        boolean macos = isMACOS();
        // TODO fix this so Exit and About in menubar?
        //        Object apple = System.getProperty("com.apple.macos.useScreenMenuBar");
        //        if (isMACOS() && (apple != null)) {
        //            macos = Boolean.getBoolean(apple.toString());
        //        }
        //        Logger.getRootLogger().warn(macos + " " + apple.toString());
        if (item instanceof JMenu) {
            JMenu menu = (JMenu) item;
            JMenu found = findMenu(parent, menu);
            if (found == null) {
                insertMenu(parent, item, macos);
            } else {
                while (menu.getItemCount() != 0) {
                    JMenuItem newitem = menu.getItem(0);
                    menu.remove(0);
                    addSubMenu(found, newitem);
                }
            }
        } else {
            if (parent instanceof JMenuBar) {
                JMenu ext = new JMenu("EXT");
                JMenu found = findMenu(parent, ext);
                if (found == null) {
                    ext.add(item);
                    addSubMenu(parent, ext);
                } else {
                    addSubMenu(found, item);
                }
            } else {
                insertMenu(parent, item, macos);
            }
        }
    }

    /**
     * Search the parent to find an existing menu with the same name.
     *
     * @param parent the menu to search.
     * @param menu   the menu to search for.
     * @return null if no menu was found, or the menu that matches the searched
     *         for menu.
     */
    static private JMenu findMenu(Object parent, JMenu menu) {
        if (parent instanceof JMenuBar) {
            JMenuBar menubar = (JMenuBar) parent;

            for (int i = 0; i < menubar.getMenuCount(); i++) {
                if (menu.getText().equals(menubar.getMenu(i).getText())) {
                    return menubar.getMenu(i);
                }
            }
            return null;
        } else {
            JMenu menubar = (JMenu) parent;

            for (int i = 0; i < menubar.getItemCount(); i++) {
                JMenuItem submenu = menubar.getItem(i);
                if ((submenu instanceof JMenu) && (menu.getText().equals(submenu.getText()))) {
                    return (JMenu) submenu;
                }
            }
            return null;
        }
    }

    /**
     * Insert the menu in the right place. This function is called after the
     * check is done to see if any menus match. This will insert the menuitem in
     * the menu structure, makeing sure certain items will stay at the bottom of
     * their respective menus (like File->exit).
     *
     * @param parent to which the menu needs to be added.
     * @param item   the menuitem that needs to be added.
     * @param macos  is this running on a Mac?
     */
    static private void insertMenu(Object parent, JMenuItem item, boolean macos) {
        if (item == null) {
            return;
        }
        if (parent instanceof JMenuBar) {
            JMenuBar menubar = (JMenuBar) parent;
            for(int i=0; i<menubar.getMenuCount(); i++) {
                JMenu menu = menubar.getMenu(i);
                if (menu.getText().equals("File")) {
                	continue;
                }
                if (menu.getText().equals("Edit")) {
                	continue;
                }
                if (menu.getText().equals("Help")) {
                	menubar.add(item, i);
                    return;
                }
                if (menu.getText().compareTo(item.getText()) > 0) {
                    menubar.add(item, i);
                    return;
                }
            }
            menubar.add(item, menubar.getMenuCount());
        } else {
            JMenu menu = (JMenu) parent;
            if (menu.getText().equals("Help") && !macos) {
                for(int i=1; i<menu.getItemCount()-2; i++) {
                    JMenuItem menuitem = menu.getItem(i);
                    if (menuitem.getText().compareTo(item.getText()) > 0) {
                        menu.add(item, i);
                        return;
                    }
                }
                menu.insert(item, menu.getItemCount() - 2);
            } else if (menu.getText().equals("File")) {
                int last = menu.getItemCount() - (macos ? 5 : 6);
                if (last == 6) {
                    menu.add(new JPopupMenu.Separator(), 6);
                    last++;
                }
                for(int i=7; i<last; i++) {
                    JMenuItem menuitem = menu.getItem(i);
                    if (menuitem.getText().compareTo(item.getText()) > 0) {
                        menu.add(item, i);
                        return;
                    }
                }
                menu.insert(item, last);
            } else {
                for(int i=0; i<menu.getItemCount(); i++) {
                    JMenuItem menuitem = menu.getItem(i);
                    if (menuitem.getText().compareTo(item.getText()) > 0) {
                        menu.add(item, i);
                        return;
                    }
                }
                menu.add(item);
            }
        }
    }

    /**
     * Create a copy of the object and all linked parts of the object. In case of an
     * array this will make a copy of the array and all objects that the array points
     * to.
     * 
     * @param object the object to be copied.
     * @return the complete copied object.
     */
    static public Object deepclone(Object object) {
        if (object == null) {
            return null;
        }

        try {
            // First serialize the properties
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(object);
            out.close();

            // Get the bytes of the serialized object
            byte[] bytes = bos.toByteArray();

            // Deserialize from a byte array
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes));
            Object result = in.readObject();
            in.close();

            // return the result
            return result;
        } catch (IOException exc) {
            return null;
        } catch (ClassNotFoundException exc) {
            return null;
        }
    }
}
