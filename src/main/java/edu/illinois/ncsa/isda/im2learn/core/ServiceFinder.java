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
package edu.illinois.ncsa.isda.im2learn.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Enumeration;

import edu.illinois.ncsa.isda.im2learn.core.display.Im2LearnMenu;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageReader;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageWriter;


public class ServiceFinder {
    
    static private Class defaults[] = new Class[] {
        Im2LearnMenu.class, ImageReader.class, ImageWriter.class
    };

    /**
     * Search the first argument for classes that implement a certain interface
     * and write the found classes to the services directory.
     * 
     * @param args classes directory and 0 or more interfaces
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("usage ServiceFinder classdir [interface ...]");
            System.exit(-1);
        }
        
        // set up the resourcelocator
        ResourceLocator rl = ResourceLocator.getInstance();
        rl.addPath(args[0], true);
        
        // create the service dir
        File services = new File(args[0] + "/META-INF/services");
        services.mkdirs();
        
        // search and create
        if (args.length == 1) {
            for(int i=0; i<defaults.length; i++) {
                find(defaults[i], services, rl);   
            }
        } else {
            for(int i=1; i<args.length; i++) {
                try {
                    Class clazz = rl.loadClass(args[i]);
                    find(clazz, services, rl);
                } catch (ClassNotFoundException exc) {
                    exc.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Write to the services directory a file that contains all those classes
     * that implement the given interface.
     * 
     * @param clazz the interface to search for
     * @param services the services directory to write the results to
     * @param rl the resourcelocator to use
     */
    private static void find(Class clazz, File services, ResourceLocator rl) {
        Enumeration found = rl.searchResources(clazz);
        if (found.hasMoreElements()) {
            File filename = new File(services, clazz.getName());
            try {
                PrintStream ps = new PrintStream(new FileOutputStream(filename));
                while(found.hasMoreElements()) {
                    Class foundclass = (Class)found.nextElement();
                    if (!foundclass.isInterface()) {
                        ps.println(foundclass.getName());
                    }
                }
                ps.close();
            } catch (FileNotFoundException exc) {
                exc.printStackTrace();
            }
        }
    }

}
