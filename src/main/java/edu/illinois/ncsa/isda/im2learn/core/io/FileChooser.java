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

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JFileChooser;

import edu.illinois.ncsa.isda.imagetools.core.Im2LearnUtilities;


/**
 * This class will present the user with a dialog to open or save a file or an
 * image. If an HDF file is selected a second dialog will appear asking the user
 * for the file inside the HDF file.
 */
public class FileChooser {
    private String initialfile = null;
    private String title = null;
    private String filter = null;

	static private Vector<LoadSaveCheck> checkers = new Vector<LoadSaveCheck>();
    static private Vector<SimpleFilter> openfilters = new Vector<SimpleFilter>();
    static private Vector<SimpleFilter> savefilters = new Vector<SimpleFilter>();
    static private String initialdir = null;
    static private Frame frame = new Frame();
    static private JFileChooser chooser = new JFileChooser(initialdir);
    
    /**
     * Default constructor.
     */
    public FileChooser() {
    }

    public void setSelectedFile(String file) {
        initialfile = file;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    /**
     * Shows a dialog allowing the user to select a filename. Based on
     * useAWTFileDialog (@see useAWTFileDialog) either the AWT or the Swing
     * version is shown. If a HDF file is selected a second dialog is shown
     * allowing the user to select the filename inside the HDF file.
     *
     * @return the full path to a file selected.
     * @throws IOException if an errror occurs showing the dialog.
     */
    public String showOpenDialog() throws IOException {
    	chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (Im2LearnUtilities.useAWTFileDialog()) {
            return showDialogAWT(true);
        } else {
        	String[] filename = showDialogSwing(true, false);
        	if (filename != null) {
        		return filename[0];
        	}
        	return null;
        }
    }
    
    /**
     * Shows a dialog allowing the user to select multiple filenames. Only Swing
     * version is shown.
     *
     * @return the array of full paths to selected files .
     * @throws IOException if an errror occurs showing the dialog.
     */
    public String[] showMultiOpenDialog() throws IOException {
    	chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        return showDialogSwing(true, true);
 
    }
    
    /**
     *	Shows a dialog allowing the user to select a directory.
     *
     * @return selected directory name
     * @throws IOException if an error occurs showing the dialog. 
     */
    public String showDirectoryOpenDialog() throws IOException{
    	chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (Im2LearnUtilities.useAWTFileDialog()) {
            return showDialogAWT(true);
        } else {
        	String[] filename = showDialogSwing(true, false);
        	if (filename != null) {
        		return filename[0];
        	}
        	return null;
        }
    }
    
    /**
     *	Shows a dialog allowing the user to select multiple directories.
     *
     * @return selected directory names
     * @throws IOException if an error occurs showing the dialog. 
     */
    public String[] showMultiDirectoryOpenDialog() throws IOException{
    	chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    	
    	String[] filename = showDialogSwing(true, true);
    	return filename;
    }
    
    
    /**
     * Shows a dialog allowing the user to select a filename. Based on
     * useAWTFileDialog (@see useAWTFileDialog) either the AWT or the Swing
     * version is shown. If a HDF file is selected a second dialog is shown
     * allowing the user to select the filename inside the HDF file.
     *
     * @return the full path to a file selected.
     * @throws IOException if an errror occurs showing the dialog.
     */
    public String showSaveDialog() throws IOException {
        if (Im2LearnUtilities.useAWTFileDialog()) {
            return showDialogAWT(false);
        } else {
        	String[] filename = showDialogSwing(false, false);
        	if (filename != null) {
        		return filename[0];
        	}
        	return null;
        }
    }

    public String getFilter() {
        return filter;
    }

    public JFileChooser getJFileChooser() {
    	return chooser;
    }
    	
    /**
     * Shows the Swing filechooser.
     *
     * @param load is true if the openfiledialog should be shown.
     * @param isMultiple is true if multiple files can be selected
     * @return the array of full paths to selected files.
     */
    private String[] showDialogSwing(boolean load, boolean isMultiple) {
        int result;

        
        SimpleFilter allimg = new SimpleFilter(null, "All Images");

        if (title != null) {
            chooser.setDialogTitle(title);
        }
        if (initialfile != null) {
            chooser.setSelectedFile(new File(initialfile));
        }

        // add user filters
        chooser.resetChoosableFileFilters();
        if (load) {
	        for (SimpleFilter filter : openfilters) {
	            chooser.addChoosableFileFilter(filter);
	            allimg.addExtentions(filter.getExtentions());
	        }

            // add Im2Learn filters
            for(ImageReader reader : ImageLoader.getReaders()) {
                SimpleFilter filter = new SimpleFilter(reader.readExt(), reader.getDescription());
	            chooser.addChoosableFileFilter(filter);
                allimg.addExtentions(filter.getExtentions());            	
            }
        } else {
	        for (SimpleFilter filter : savefilters) {
	            chooser.addChoosableFileFilter(filter);
	            allimg.addExtentions(filter.getExtentions());
	        }

	        // add Im2Learn filters
            for(ImageWriter writer : ImageLoader.getWriters()) {
                SimpleFilter filter = new SimpleFilter(writer.writeExt(), writer.getDescription());
	            chooser.addChoosableFileFilter(filter);
                allimg.addExtentions(filter.getExtentions());            	
            }
        }

        // add the all image filter last
        chooser.addChoosableFileFilter(allimg);

        while (true) {
            if (load) {
           		chooser.setMultiSelectionEnabled(isMultiple);
                result = chooser.showOpenDialog(null);
            } else {
                result = chooser.showSaveDialog(null);
            }
            if (result != JFileChooser.APPROVE_OPTION) {
                initialfile = null;
                return null;
            }

            if ((chooser.getFileFilter() != chooser.getAcceptAllFileFilter()) &&
                (chooser.getFileFilter() != allimg)) {
                filter = chooser.getFileFilter().getDescription().split(" \\(")[0];
            }
            
            if(isMultiple) {
		        File[] file = chooser.getSelectedFiles();
		        String[] filename = new String[file.length]; 
		        for(int i=0; i<file.length; i++) {
		        	filename[i] = file[i].getAbsolutePath();
					filename[i] = check(filename[i], load);
		        }
		        initialfile = null;
		        initialdir = chooser.getSelectedFile().getPath();
		        return filename;
            }
            else {
		        String[] filename = new String[1]; 
		        filename[0] = chooser.getSelectedFile().getAbsolutePath();
		    	filename[0] = check(filename[0], load);
		        initialfile = null;
		        initialdir = chooser.getSelectedFile().getPath();
		        return filename;
            }
        }
    }

    /**
     * Shows the AWT filechooser.
     *
     * @param load is true if the openfiledialog should be shown.
     * @return the full path to a file selected.
     */
    private String showDialogAWT(boolean load) {
        FileDialog chooser = new FileDialog(frame);
        if (load) {
            chooser.setMode(FileDialog.LOAD);
        } else {
            chooser.setMode(FileDialog.SAVE);
        }
        SimpleFilter allimg = new SimpleFilter(null, "All Images");

        if (title != null) {
            chooser.setTitle(title);
        }
        if (initialfile != null) {
            chooser.setFile(initialfile);
        }

        // add user filters
        if (load) {
            for (SimpleFilter filter : openfilters) {
                allimg.addExtentions(filter.getExtentions());
            }        	

            // add Im2Learn filters
            for(ImageReader reader : ImageLoader.getReaders()) {
                SimpleFilter filter = new SimpleFilter(reader.readExt(), reader.getDescription());
                allimg.addExtentions(filter.getExtentions());            	
            }
        } else {
            for (SimpleFilter filter : savefilters) {
                allimg.addExtentions(filter.getExtentions());
            }        	

            // add Im2Learn filters
            for(ImageWriter writer : ImageLoader.getWriters()) {
                SimpleFilter filter = new SimpleFilter(writer.writeExt(), writer.getDescription());
                allimg.addExtentions(filter.getExtentions());            	
            }
        }


        // add the all image filter last
        chooser.setFilenameFilter(allimg);

        while (true) {
            chooser.setVisible(true);
            if (chooser.getFile() == null) {
                initialfile = null;
                return null;
            }

            String filename = chooser.getDirectory() + chooser.getFile();
			filename = check(filename, load);
            initialfile = null;
            initialdir = chooser.getDirectory();
            return filename;
        }
    }

    static public void setInitialDirectory(String initialdir) {
        FileChooser.initialdir = initialdir;
    }

    static public String getInitialDirectory() {
        return FileChooser.initialdir;
    }
    
    
    
    static public void addOpenFilter(String[] extentions, String description) {
        openfilters.add(new SimpleFilter(extentions, description));
    }

    static public void addSaveFilter(String[] extentions, String description) {
        savefilters.add(new SimpleFilter(extentions, description));
    }

    static public void clearOpenFilter() {
        openfilters.removeAllElements();
    }

    static public void clearSaveFilter() {
    	savefilters.removeAllElements();
    }

	static public void addChecker(LoadSaveCheck check) {
        if (check == null) {
            return;
        }

        // make sure we only add once
        for (LoadSaveCheck c : checkers) {
        	if (c.getClass().isInstance(check)) {
        		return;
        	}
        }

        checkers.add(check);
	}

	static public void removeChecker(LoadSaveCheck check) {
		checkers.remove(check);
	}

	static public String check(String filename, boolean load) {
		for(LoadSaveCheck checker : checkers) {
			filename = checker.check(filename, load);
		}
		return filename;
	}
}
