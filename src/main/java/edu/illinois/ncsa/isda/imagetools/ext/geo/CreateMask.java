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


import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.lang.Math;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.datatype.*;
import edu.illinois.ncsa.isda.imagetools.core.display.ImagePanel;
import edu.illinois.ncsa.isda.imagetools.core.display.ImageUpdateEvent;
import edu.illinois.ncsa.isda.imagetools.core.geo.GeoException;
import edu.illinois.ncsa.isda.imagetools.core.geo.projection.*;
import edu.illinois.ncsa.isda.imagetools.core.io.ImageLoader;
import edu.illinois.ncsa.isda.imagetools.core.io.shapefile.ShapefileLoader;
import edu.illinois.ncsa.isda.imagetools.ext.misc.ImEnhance;

/**
 * 
 * @author Qi Li
 *
 */

public class CreateMask {
    
	public static ShapeObject shp = null;
	public static ShapeObject imgShape = null;
	public static TableModel table = null;
	public static Log logger = LogFactory.getLog(CreateMask.class);
	public static double radius;
	public static int whiteCounts =0;
	public static int blackCounts =0;
	public static String type = null;
	public static final String TYPE_POINT = "csv";
	public static final String TYPE_EXCEL = "excel";
	public static final String TYPE_SHAPE = "shape";
	public static boolean isInclude = true;
	
	public static ArrayList<int[]> selected;
	public static boolean [] rowsSelected;
	//the upper left corner pixel(the first pixel), which gives the relative position of this image in the big baseline image. point.x = row, point.y = col
	
	public static ImageObject createMask(ImageObject imgobj, TableModel tm, int radius, int latcol, int loncol, boolean include) throws IllegalArgumentException, GeoException {
		selected = new ArrayList<int[]>();
		ImageObject mask = null;
		rowsSelected = new boolean [tm.getRowCount()];
		//mask =  new ImageObjectByte(rows, cols, 1);
		try {
			int initialColor;
			int finalColor;
			int rows = imgobj.getNumRows();
			int cols = imgobj.getNumCols();
			Projection proj = (Projection)imgobj.getProperty(ImageObject.GEOINFO);
			mask = ImageObject.createImage(rows,cols,1,ImageObject.TYPE_BYTE);
			mask.setProperty(ImageObject.GEOINFO, proj);
			if(include){
				initialColor = 0;
				finalColor = 255;
				whiteCounts = 0;
				blackCounts = mask.getSize();
			}else{
				initialColor = 255;
				finalColor = 0;
				whiteCounts = mask.getSize();
				blackCounts = 0;
				isInclude = false;
			}
			mask.setImageObjectValue(initialColor);
			double lat, lon;
			//double scaleX = proj.getScaleX();
			//double scaleY = proj.getScaleY();
			//int rangeX,rangeY;
			int points=0;int pointsInMask=0;
			for(int i=0;i<tm.getRowCount();i++,points++){
				lat = Double.parseDouble(tm.getValueAt(i,latcol).toString());
				lon = Double.parseDouble(tm.getValueAt(i,loncol).toString());
				double[] point = new double[]{lat,lon};
				double[] colRow = proj.earthToRaster(point);
				int row = (int)colRow[1];
				int col = (int)colRow[0];
				if(!checkSelected(row, col) && (row >= 0 && row <= imgobj.getNumRows()-1)
						&& (col >= 0 && col <= imgobj.getNumCols()-1)){
					int[] toAdd = new int[3];
					toAdd[0] = row;
					toAdd[1] = col;
					toAdd[2] = i;
					selected.add(toAdd);
					rowsSelected[i] = true;
					if((row>=0)&&(row<rows)&&(col>=0)&&(col<cols)){
						/*if (radius > 0) {
							rangeX = (int)((radius-radius%scaleX)/scaleX+1);
							rangeY = (int)((radius-radius%scaleY)/scaleY+1);
							for(int m=row-rangeX;m<=row+rangeX;m++){
								for(int n=col-rangeY;n<=col+rangeY;n++){
									if ((m>=0)&&(m<rows)&&(n>=0)&&(n<cols)){
										double r = Math.hypot(Math.abs(row-m)*scaleX,Math.abs(col-n)*scaleY);
										if(r*r<=radius*radius){
											mask.set(startPt.x + m,startPt.y + n,0,finalColor);
											if(include){
												whiteCounts++;
												blackCounts--;
											}else{
												whiteCounts--;
												blackCounts++;
											}
										}
									}
								}
							}
							
						}*/ 
						if(radius > 0 ){
							for(int m=row-radius;m<=row+radius;m++){
								for(int n=col-radius;n<=col+radius;n++){
									if(m>=0 && m<rows && n>=0 && n<cols){
										double r = Math.hypot(Math.abs(row-m),Math.abs(col-n));
										if(r*r<=radius*radius){
											mask.set(m,n,0,finalColor);
											if(include){
												whiteCounts++;
												blackCounts--;
											}else{
												whiteCounts--;
												blackCounts++;
											}
									}
									}
									
							}
							}
						}else {
							mask.set(row,col,0,finalColor);
						}
						pointsInMask++;
						}
				}else{
					rowsSelected[i] = false;
				}
			}
			System.out.println("total points = " + points);
			System.out.println("points in mask = " + pointsInMask);
		} catch (ImageException e) {
			e.printStackTrace();
		}
		return mask;
	}
	
	public static ImageObject createMask(ImageObject imgobj, TableModel tm, int radius, boolean include) throws IllegalArgumentException, GeoException{
		return createMask(imgobj, tm, radius, 1, 2, include);
	}
	
	public static ImageObject createMask(ImageObject imgobj, TableModel tm, int radius) throws Exception, GeoException{
		return createMask(imgobj, tm, radius, 1, 2, true);
	}
	
	public static ImageObject createMask(ImageObject imgobj, TableModel tm) throws ImageException {
		return createMask(imgobj, tm, 0, 1, 2, true);
	}
	
	public static ImageObject createMask(ImageObject image, String filename, int radius, int latcol, int loncol, boolean include,int sheet,boolean header) throws Exception {
		CreateMask.radius = radius;
		if(filename.endsWith(".csv")){
			table = new CSVTable(filename).table;
			type = "csv";
			return createMask(image, table, radius, latcol, loncol, include);
		}
		else if(filename.endsWith(".xls")) {
			table = new ExcelTable().excelToTable(filename,sheet,header);
			type = "excel";
			return createMask(image, table, radius, latcol, loncol, include);
		}
		else if(filename.endsWith(".shp")){
			ShapefileLoader shapeloader = new ShapefileLoader(filename);
            shp = shapeloader.getShapeObject();
            type = "shape";
			return createMask(image,shp);
		}
		else{
			System.out.println("Only CSV, Excel and shape files are supported!");
	        return image;
		}
	}
	
	public static ImageObject createMask(ImageObject image, String filename, int radius, boolean include) throws Exception{
		return createMask(image, filename, radius, 1, 2, include,0,true);
	}
	
	public static ImageObject createMask(ImageObject image, String filename, int radius) throws Exception{
		return createMask(image, filename, radius, 1, 2, true,0,true);
	}
	
	public static ImageObject createMask(ImageObject image, String filename) throws Exception{
		return createMask(image, filename, 0, 1, 2, true,0,true);
	}

	public static ImageObject createMask(ImageObject raster, ShapeObject shape) throws ImageException{
		
		ImageObject mask = null;
		GeoFeature geoFeature = new GeoFeature();
        imgShape = geoFeature.ConstructImageShapeObject(raster, shape);
        mask= geoFeature.ConstructMaskOut(raster, imgShape);
        ImEnhance enhance = new ImEnhance();
        if (enhance.EnhanceLabelsIn(mask, -1, true)) {
            mask = enhance.GetEnhancedObject();
            mask.setProperty(ImageObject.GEOINFO, raster.getProperty(ImageObject.GEOINFO));
        }
        type = "shape";
		return mask;
	}
	
	public static TableModel readTable(String file) throws Exception{
		TableModel t = new DefaultTableModel();
		if(file.endsWith(".csv")){
			t = (new CSVTable()).readTable(file);
		}
		if(file.endsWith(".xls")){
			t = ExcelTable.excelToTable(file,0);
		}
		return t;
	}
	
	public static boolean checkSelected(int row, int col){
		boolean alreadyIn = false;
		for(int[] pt : selected){
			if(selected.size() > 0 && pt[0] == row && pt[1] == col){
				alreadyIn = true;
				return alreadyIn;
			}
		}
		return alreadyIn;
	}
	
	public String getType(){
		return type;
	}
	
	public ShapeObject getShape(){
		return shp;
	}
	
	public ShapeObject getimgShape(){
		return imgShape;
	}
	
	public TableModel getTable(){
		return table;
	}
	
	public void setShape(ShapeObject shape){
		shp = shape;
		return;
	}
	
	public void setTable(TableModel t){
		table = t;
		return;
	}
	
}
