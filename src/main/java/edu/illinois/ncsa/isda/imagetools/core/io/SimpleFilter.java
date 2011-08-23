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
package edu.illinois.ncsa.isda.imagetools.core.io;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Vector;

/**
 * Simple filter based on extentions.
 */
public class SimpleFilter extends FileFilter implements FilenameFilter {
    private String description = "no files";
    private Vector<String> extentions = new Vector<String>();

    /**
     * Create a simple filter with given extentions and description.
     *
     * @param extentions  is list of extentions to accept.
     * @param description of the filter.
     */
    public SimpleFilter(String[] extentions, String description) {
        if (description != null) {
            this.description = description;
        }
        addExtentions(extentions);
    }

    public void addExtentions(String[] extentions) {
        if ((extentions == null) || (extentions.length == 0)) {
            return;
        }

        for (int i = 0; i < extentions.length; i++) {
            String ext = extentions[i].toLowerCase();
            if (!this.extentions.contains(ext)) {
                this.extentions.add(ext);
            }
        }
    }

    public void addExtentions(Vector<String> extentions) {
        if ((extentions == null) || (extentions.size() == 0)) {
            return;
        }

        for (int i = 0; i < extentions.size(); i++) {
            String ext = extentions.get(i).toString().toLowerCase();
            if (!this.extentions.contains(ext)) {
                this.extentions.add(ext);
            }
        }
    }

    /**
     * Whether the given file is accepted by this filter.
     *
     * @param file which to check.
     * @return true if file is accepted by filter.
     */
    public boolean accept(File file) {
        if (file.isDirectory()) {
            return true;
        }

        String ext = getExtension(file.getName());
        return extentions.contains(ext);
    }

    /**
     * Tests if a specified file should be included in a file list.
     *
     * @param dir  the directory in which the file was found.
     * @param name the name of the file.
     * @return <code>true</code> if and only if the name should be included in
     *         the file list; <code>false</code> otherwise.
     */
    public boolean accept(File dir, String name) {
        String ext = getExtension(name);
        return extentions.contains(ext);
    }

    /**
     * Returns a list of extentions that are accepted by this filter.
     *
     * @return list of extentions.
     */
    public Vector<String> getExtentions() {
        return extentions;
    }

    /**
     * Return the description of the filter.
     *
     * @return description of filter.
     */
    public String getDescription() {
        if (extentions.size() == 0) {
            return description + " ( none )";
        }
        if (extentions.size() > 5) {
            return description + " ( many )";
        }

        String result = description + " (*." + extentions.get(0).toString();
        for (int i = 1; i < extentions.size(); i++) {
            result += ", *." + extentions.get(i).toString();
        }
        result += ")";
        return result;
    }

    /**
     * Get the extension of a file.
     *
     * @param file of which to return extention.
     * @return extention.
     */
    private String getExtension(String file) {
/*        String ext = null;
        int i = file.lastIndexOf('.');

        if (i > 0 && i < file.length() - 1) {
            ext = file.substring(i + 1).toLowerCase();
        }
        return ext;*/

        return ImageLoader.getExtention(file);
    }
}


/**
 * 4/28/2005 clutter Changed getExtension() to call ImageLoader.getExtension().
 */
