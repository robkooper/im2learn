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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Search all jar files for resources and keep a list of them for later
 * retrieval.
 *
 * @author Rob Kooper
 * @version 1.0
 */
public class ResourceLocator extends URLClassLoader {
    private ArrayList urls;
    private ArrayList resources;
    private boolean parent;
    private Pattern filter = null;
    private boolean showconflict = false;

    static private Log logger = LogFactory.getLog(ResourceLocator.class);

	static private ResourceLocator instance = null;
	
	static public ResourceLocator getInstance() {
		if (instance == null) {
			instance = new ResourceLocator();
		}
		return instance;
	}

	/**
     * Create classloader, this will have no extra paths and will be just the
     * parent classloader.
     */
    private ResourceLocator() {
        super(new URL[0]);

        this.resources = new ArrayList();
        this.urls = new ArrayList();
    }

    // -----------------------------------------------------------------------
    // add path and urls to already existing list and search for resources.
    // -----------------------------------------------------------------------

    /**
     * Add url to list of urls to search for classes and resources.
     *
     * @param url the url to be added to the list.
     */
    public void addURL(URL url, boolean search) {
        if (url == null) {
            return;
        }
        super.addURL(url);
        if (search) {
	        this.urls.add(url);
	        checkURL(url);
        }
    }

    /**
     * Add all the paths in the classpath to the list of urls to be searched for
     * classes and resources.
     *
     * @param path containing multiple urls seperated by File.pathSeparator.
     */
    public void addPath(String path, boolean search) {
        if (path == null) {
            return;
        }
        List urls = new ArrayList();
        makeClassPathFile(path, urls);
        for (int i = 0; i < urls.size(); i++) {
            URL url = (URL) urls.get(i);
            super.addURL(url);
            if (search) {
	            this.urls.add(url);
	            checkURL(url);
            }
        }
    }

    /**
     * Add all the resources located in the parent classpath. This will grow the
     * list of resources enormously since this will incorporate the full runtime
     * as well.
     */
    public void addParentResources() {
        if (parent) {
            return;
        }
        parent = true;

        ClassLoader cl = getParent();
        if (cl instanceof URLClassLoader) {
            URLClassLoader ucl = (URLClassLoader) cl;
            URL[] urls = ucl.getURLs();
            for (int i = 0; i < urls.length; i++) {
                checkURL(urls[i]);
            }
        }
    }

    private void checkURL(URL url) {
        logger.debug("Checking " + url);
        synchronized (resources) {
            if (url.getFile().endsWith(".jar")) {
                listJarResources(url);
            } else if (url.getProtocol().equals("file")) {
                File file = new File(url.getFile());
                if (file.isDirectory()) {
                    listDirResources(file, "");
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // set / reset filter
    // -----------------------------------------------------------------------

    /**
     * Return the current filter in place. This is the filter that is currently
     * used when a new url is added to the classloader.
     *
     * @return current filter that is in use.
     */
    public String getFilter() {
        return filter.pattern();
    }

    /**
     * Set the filter that is in use. Any new resources added will be checked
     * with this filter. Once a resource has been filtered out it can not be
     * undone, until a reset is called with a different filter.
     * <p/>
     * If the variable apply is set, the filter will be applied to the already
     * found resources.
     * <p/>
     * If addParentResources() is called setting the filter to
     * "^((javax?)|(sun))\\." will filter most of the java and sun classes out.
     *
     * @param regex the filter to apply to any new resources located.
     * @param apply if set to true, apply the filter to alread loaded
     *              resources.
     */
    public void setFilter(String regex, boolean apply) {
        if (regex == null) {
            filter = null;
        } else {
            filter = Pattern.compile(regex);
        }

        if (apply) {
            synchronized (resources) {
                for (Iterator iter = resources.iterator(); iter.hasNext();) {
                    String resource = (String) iter.next();
                    if (filter.matcher(resource).find()) {
                        logger.debug("Filtering " + resource);
                        iter.remove();
                    }
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // return the resources or reset the resources to the original state.
    // -----------------------------------------------------------------------

    /**
     * Return a list of resources found in the classpath of this classloader.
     *
     * @return a list of resources located in the classpath of this
     *         classloader.
     */
    public List getResources() {
        return resources;
    }

    /**
     * Reset the list of resources. This is usefull if the parent resources were
     * added to the resource list. Or if a filter was applied and a new filter
     * needs to be applied.
     */
    public void reset() {
        synchronized (resources) {
            resources.clear();
            parent = false;
        }
        for (int i = 0; i < urls.size(); i++) {
            checkURL((URL) urls.get(i));
        }
    }

    // -----------------------------------------------------------------------
    // search the resources for a specific resource or set of resources.
    // -----------------------------------------------------------------------

    /**
     * Search all resources for classes that are assignable to the given class.
     * This function will return an enumeration. The actual check for classes
     * that are asignable is done during evaluation of the enumeration.
     *
     * @param assignable the class to check if is assignable to.
     * @return enumeration which at runtime will check for assignability.
     */
    public Enumeration searchResources(Class assignable) {
        // get all the classes
        final Enumeration enumeration = searchResources("\\.class$");
        final Class clazz = assignable;

        // create enumeration to return
        return new Enumeration() {
            private Object next = null;

            public Object nextElement() {
                hasMoreElements();
                Object obj = next;
                next = null;
                return obj;
            }

            public boolean hasMoreElements() {
                String classname;
                Class found;

                while ((next == null) && enumeration.hasMoreElements()) {
                    String resource = (String) enumeration.nextElement();
                    classname = resource.substring(0, resource.length() - 6);
                    try {
                        found = loadClass(classname);
                        if (clazz.isAssignableFrom(found)) {
                            next = found;
                        }
                    } catch (Throwable thr) {
                        //logger.debug("Error loading class : " + name, thr);
                    }
                }

                return (next != null);
            }
        };
    }

    /**
     * Search all the resources for a match. To get a list of all gif images for
     * instance call with searchResources("\\.gif$").
     *
     * @param regex the regular expresson used to filter the returned list
     * @return an enumeration of all matches.
     */
    public Enumeration searchResources(String regex) {
        Pattern pattern = Pattern.compile(regex);
        ArrayList result = new ArrayList();

        // search resources for all matches
        synchronized (resources) {
            for (Iterator iter = resources.iterator(); iter.hasNext();) {
                String resource = (String) iter.next();
                Matcher matcher = pattern.matcher(resource);
                if (matcher.find()) {
                    result.add(resource);
                }
            }
        }

        // create enumeration to return
        final Iterator iter = result.iterator();
        return new Enumeration() {
            public Object nextElement() {
                return iter.next();
            }

            public boolean hasMoreElements() {
                return iter.hasNext();
            }
        };
    }

    // -----------------------------------------------------------------------
    // list all jar files in a directory or split a classpath up in seperate
    // parts.
    // -----------------------------------------------------------------------

    private void makeClassPathFile(String filename, List urls) {
        if (filename == null) {
            return;
        }

        String[] path = filename.split(File.pathSeparator);
        for (int i = 0; i < path.length; i++) {
            File file = new File(path[i]);
            if (file.exists()) {
                try {
                    urls.add(file.toURL());
                } catch (MalformedURLException exc) {
                    logger.warn("Could not convert file to url.", exc);
                }
                if (file.isDirectory()) {
                    makeClassPathDir(file, urls);
                }
            }
        }
    }


    private void makeClassPathDir(File dir, List urls) {
        if (dir == null) {
            return;
        }

        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            String name = files[i].getName();
            if (!name.equals(".") && !name.equals("..") &&
                name.toLowerCase().endsWith(".jar")) {
                try {
                    urls.add(files[i].toURL());
                } catch (MalformedURLException exc) {
                    logger.warn("Could not convert file to url.", exc);
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // list the resources in the class path and the jar files.
    // -----------------------------------------------------------------------

    private void listDirResources(File dir, String fqdn) {
        if (!fqdn.equals("")) {
            fqdn += ".";
        }
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            String name = fqdn + file.getName();
            if ((filter == null) || !filter.matcher(name).find()) {
                if (file.isDirectory()) {
                    listDirResources(file, name);
                } else {
                    if (showconflict && resources.contains(name)) {
                        logger.debug(name + " already included in resources.");
                    }
                    resources.add(name);
                }
            }
        }
    }


    private void listJarResources(URL url) {
        try {
            URL jarUrl = new URL("jar:" + url.toExternalForm() + "!/");
            JarURLConnection jarConnection = (JarURLConnection) jarUrl.openConnection();
            JarFile jarfile = jarConnection.getJarFile();

            for (Enumeration e = jarfile.entries(); e.hasMoreElements();) {
                JarEntry entry = (JarEntry) e.nextElement();
                if (!entry.isDirectory()) {
                    String name = entry.getName().replace('/', '.');
                    if ((filter == null) || !filter.matcher(name).find()) {
                        if (showconflict && resources.contains(name)) {
                            logger.debug(name + " already included in resources.");
                        }
                        resources.add(name);
                    }
                }
            }
        } catch (IOException exc) {
            exc.printStackTrace();
        }
    }
}
