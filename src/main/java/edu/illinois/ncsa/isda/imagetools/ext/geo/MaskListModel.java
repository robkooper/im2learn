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
package edu.illinois.ncsa.isda.imagetools.ext.geo;

import java.io.File;
import java.util.ArrayList;

import javax.swing.AbstractListModel;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageException;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.geo.projection.Projection;
import edu.illinois.ncsa.isda.imagetools.core.io.ImageLoader;

public class MaskListModel extends AbstractListModel{


    static private Log logger = LogFactory.getLog(MaskListModel.class);
    
    private Projection projection = null;

    public ArrayList<ImageObject> masks;
    public ArrayList<String> names;

    public MaskListModel() {
        masks = new ArrayList<ImageObject>();
        names = new ArrayList<String>();
    }
    
    public void addMask(ImageObject imgobj, String name){
        try {
        	Projection prj = (Projection) imgobj.getProperty(ImageObject.GEOINFO);
        	 if (prj == null) {
        		 logger.info("The image added to the list does not have projection info.");
                 //throw (new ImageException());
             }
        	ImageObject cloned = (ImageObject)imgobj.clone();
        	if(projection!=null){
        		cloned.setProperty(ImageObject.GEOINFO, projection);
        	}else{
        		logger.info("There is no valid projection type to be set to the image being added to list.");
        	}
        	name = setMaskName(name);
        	/*String[] variable = new String[imgobj.getNumBands()];
        	name = setMaskName(name);
        	if (variable.length == 1) {
                variable[0] = name;
            } else {
                for (int j = 0; j < variable.length; j++) {
                    variable[0] = name + "_band_" + j;
                }
            }
        	imgobj.setProperty(GeoLearn.VARIABLENAME, variable);
            imgobj.setInvalidData(GeoLearn.NODATAVALUEFLOAT);*/
           
            int lastImg = masks.size();
            int lastname = names.size();
            masks.add(cloned);
            masks.trimToSize();
            names.ensureCapacity(masks.size());
            names.add(masks.size()-1,name);
            fireIntervalAdded(this, lastImg, masks.size());
            fireIntervalAdded(this, lastname, names.size());
        } catch (Exception e) {
            logger.warn("Could not clone imgobj.", e);
        }
    }

    public void addMask(String[] filename) {
        for (String file : filename) {
            addMask(file);
        }
    }

    public void addMask(String filename) {
        int last = masks.size();
        String name = new File(filename).getName();
        if(name.lastIndexOf('.')>0){
            name = name.substring(0,name.lastIndexOf('.'));
        }
        try {
            ImageObject imgobj = ImageLoader.readImage(filename);
            Projection prj = (Projection) imgobj.getProperty(ImageObject.GEOINFO);
            if (prj == null) {
            	logger.info("The image added to the list does not have projection info.");
            	//throw (new ImageException("No projection found in image."));
            }
            if(projection != null){
            	imgobj.setProperty(ImageObject.GEOINFO, projection);
            }else{
        		logger.info("There is no valid projection type to be set to the image being added to list.");
        	}
            name = setMaskName(name);
        	/*String[] variable = new String[imgobj.getNumBands()];
        	name = setMaskName(name);
        	if (variable.length == 1) {
                variable[0] = name;
            } else {
                for (int j = 0; j < variable.length; j++) {
                    variable[0] = name + "_band_" + j;
                }
            }
        	imgobj.setProperty(GeoLearn.VARIABLENAME, variable);
            imgobj.setInvalidData(GeoLearn.NODATAVALUEFLOAT);*/
            
            masks.add(imgobj);
            masks.trimToSize();
            names.ensureCapacity(masks.size());
            names.add(masks.size()-1,name);
        } catch (Exception exc) {
            logger.error("Could not load " + filename, exc);
        }
        fireIntervalAdded(this, last, masks.size());
    }

    public void addMasks(File[] files) {
        int last = masks.size();
        for (int i = 0; i < files.length; i++) {
            String filename = files[i].getAbsolutePath();
            try {
                ImageObject imgobj = ImageLoader.readImage(filename);
                Projection prj = (Projection) imgobj.getProperty(ImageObject.GEOINFO);
                if (prj == null) {
                	logger.info("The image added to the list does not have projection info.");
                	//throw (new ImageException("No projection found in image."));
                }
                if(projection != null){
                	imgobj.setProperty(ImageObject.GEOINFO, projection);
                }else{
            		logger.info("There is no valid projection type to be set to the image being added to list.");
            	}
                filename = setMaskName(filename);
                /*String[] variable = new String[imgobj.getNumBands()];
            	if (variable.length == 1) {
                    variable[0] = filename;
                } else {
                    for (int j = 0; j < variable.length; j++) {
                        variable[0] = filename + "_band_" + j;
                    }
                }
            	imgobj.setProperty(GeoLearn.VARIABLENAME, variable);
                imgobj.setInvalidData(GeoLearn.NODATAVALUEFLOAT);*/
                masks.add(imgobj);
                masks.trimToSize();
                names.ensureCapacity(masks.size());
                names.add(masks.size()-1,filename);
            } catch (Exception exc) {
                logger.error("Could not load " + filename, exc);
            }
        }
        fireIntervalAdded(this, last, masks.size());
    }

    public void removeMasks(int[] index) {
        int last = masks.size();
        Object[] removeImg = new Object[index.length];
        Object[] removeNames = new Object[index.length];
        for (int i = 0; i < index.length; i++) {
            removeImg[i] = masks.get(index[i]);
            removeNames[i] = names.get(index[i]);
        }
        for (int i = 0; i < removeImg.length; i++) {
            masks.remove(removeImg[i]);
            names.remove(removeNames[i]);
        }
        masks.trimToSize();
        names.trimToSize();
        fireIntervalRemoved(this, masks.size() + 1, last);//may need add string component
        fireContentsChanged(this, 0, masks.size());//same as above
    }

    public void clear() {
        int last = masks.size();
        masks.clear();
        names.clear();
        masks.trimToSize();
        names.trimToSize();
        fireIntervalRemoved(this, 0, last);//same as above
    }

    public ImageObject getMask(int index) {
        if ((index < 0) || (index >= masks.size())) {
            return null;
        }
        return masks.get(index);
    }

    public ImageObject[] getMasks() {
        return masks.toArray(new ImageObject[0]);
    }

    public int getSize() {
        return masks.size();
    }

    public Object getElementAt(int index) {
        if((index < 0) || (index >= masks.size())){
            System.err.println("There is not element at the specified index of" + Integer.toString(index));
            return null;
        }
        return names.get(index);
    }
    
    public void rename(int idx,String name){
        names.set(idx,name);
        fireContentsChanged(this, 0, masks.size());//same as above
    }
    
    public void rename(int[] idx,String name){
        for(int i : idx){
            rename(i,name);
        }
    }
    
    private String setMaskName(String name){
        String s = null;
        if(checkMaskNameAvailability(name)){
            s = name;
        }else{
            int suffix = 1;
            s = name;
            while(!checkMaskNameAvailability(s)){
                s = name + Integer.toString(suffix);
                suffix++;
            }
        }
        return s;
    }
    
    private boolean checkMaskNameAvailability(String name){
        for(String existingName : names){
            if(name.equalsIgnoreCase(existingName)){
                return false;
            }
        }
        return true;
    }
    
    public void setProjection(Projection projection){
    	if(projection != null){
    		this.projection = projection;
    	}
    }

}
