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
package edu.illinois.ncsa.isda.im2learn.ext.vis;

import java.awt.Color;

import javax.media.j3d.Canvas3D;
import javax.swing.JComponent;
import javax.vecmath.Color4f;

import com.sun.media.controls.VFlowLayout;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.display.ColorBar;



public class Image3DPanel extends JComponent{

	public static final int 
	MODE_GRAYSCALE = 0, 
	MODE_RBAND = 1, 	
	MODE_GBAND = 2,
	MODE_BBAND = 3,
	MODE_3BAND = 4,
	MODE_PSEUDO = 5,
	MODE_MAP = 6,
	MODE_3BANDMAP = 7,
	MODE_PSEUDOMAP = 8;


	float[] x,y,z;
	float[] dx,dy,dz;
	int[] x_index, y_index, z_index;
	Color4f[] color, color_vf;
	float voxelSize;
	int width, height, depth, volumeSize, validVoxelNum;
	float maxAxisSize;
	public Canvas3DPt canvas;
	public float voxelLowerThresold = 30f;
	public float voxelUpperThresold = 230f;
	boolean _isPolygon = false;

	int _band = 0;
	int gap = 100;	

	int cMax = 150, cMin = 30;
	
	int[] boundingBox = {Integer.MIN_VALUE, Integer.MAX_VALUE,
			Integer.MIN_VALUE, Integer.MAX_VALUE,
			Integer.MIN_VALUE, Integer.MAX_VALUE
	};


	public Image3DPanel (ImageObject imObj, int mode) {
		depth = imObj.getNumBands();

		switch(mode) {
		case MODE_RBAND:
			initMultiBandRGB(imObj, true, false, false);
			break;
		case MODE_GBAND:
			initMultiBandRGB(imObj, false, true, false);
			break;
		case MODE_BBAND:
			initMultiBandRGB(imObj, false, false, true);
			break;


		case MODE_3BAND:
			if(imObj.getNumBands() == 1) {
				initMultiBandRGB(imObj, true, true, true);	
			}
			else {
				init3Band(imObj, false,2);
			}
			break;

		case MODE_PSEUDO:
			initMultiBand(imObj, true);
			break;
		case MODE_GRAYSCALE:
			initMultiBand(imObj, false);
			break;


		case MODE_MAP:
			initOneBand(imObj, true, false);
			break;
		case MODE_PSEUDOMAP:
			initOneBand(imObj, true, true);
			break;

		case MODE_3BANDMAP:
			init3Band(imObj, true,2);
			break;
		}


		canvas = new Canvas3DPt();
		canvas.drawAxis = false;
		canvas.drawBoundingBox = true;
		canvas.setArray(x,y,z,voxelSize*0.5f,color,Canvas3DPt.POINT);
		// _isPolygon = true;
		//  canvas.setArray(x,y,z,voxelSize*0.5f,color,Canvas3DPt.MCUBE);

	}

	public Image3DPanel (ImageObject[] imObj) {
		if (imObj == null){
			drawEmptyPanel();
			return;
		}
		
		System.out.println("thresholds: " + voxelLowerThresold + "," + voxelUpperThresold);
		depth = imObj.length;
		initMultiFiles(imObj, false);

		canvas = new Canvas3DPt();
		canvas.drawAxis = false;
		canvas.drawBoundingBox = true;
		canvas.setArray(x,y,z,voxelSize*0.5f,color,Canvas3DPt.POINT);		
	}

	public Image3DPanel (ImageObject[] imObj, boolean pseudo) {

		depth = imObj.length;
		initMultiFiles(imObj, pseudo);

		canvas = new Canvas3DPt();
		canvas.drawAxis = true;
		canvas.drawBoundingBox = true;
		canvas.setArray(x,y,z,voxelSize*0.5f,color,Canvas3DPt.POINT);

	}
	
	public void updateImObj (ImageObject[] imObj){
		if (imObj == null){
			drawEmptyPanel();
			return;
		}
		depth = imObj.length;
		initMultiFiles(imObj, false);
		canvas.setArray(x,y,z,voxelSize*0.5f,color,canvas.getType());		
	}
	
	public void updateImObj(ImageObject[] imObj, int type){
		if (imObj == null){
			drawEmptyPanel();
			return;
		}
		depth = imObj.length;
		initMultiFiles(imObj, false);
		canvas.setArray(x,y,z,voxelSize*0.5f,color,type);
	}
	
	int getValidPixelNum(ImageObject imObj, int band) {
		float val;
		int validNum = 0;
		for(int j = 0; j<height; j++) {
			for(int i = 0; i<width; i++) {
				val = imObj.getInt(j,i,band);	
				if(val >= this.voxelLowerThresold && val <= this.voxelUpperThresold) {
					validNum++;
				}
			}
		}

		return validNum;
	}
	
	
	public void changeVisType(int type){
		canvas.setArray(x,y,z,voxelSize*0.5f,color,type);
	}

	/**
	 *	use an array of imageObjects to define the vector field 
	 *	on top of the valid voxels.
	 *
	 */
	public void addVF(ImageObject[] vf, float vfTh){
		if (vf == null){
			System.err.println("ERROR: vf is null.");
			return;
		}
	
		// initialize a sample vector field
		dx = new float[x.length];
		dy = new float[x.length];
		dz = new float[x.length];
		color_vf = new Color4f[x.length];
		float c = (float)Math.sqrt(3) * voxelSize / 255f;
		for (int i=0;i<x.length;i++){
			dx[i] = c * vf[z_index[i]].getByte(y_index[i], x_index[i], 0);
			dy[i] = c * vf[z_index[i]].getByte(y_index[i], x_index[i], 1);
			dz[i] = c * vf[z_index[i]].getByte(y_index[i], x_index[i], 2);
			color_vf[i] = new Color4f(1.0f, 0.0f, 0.0f, 0.0f);
		}
		
		if (canvas == null){
			canvas = new Canvas3DPt();
			canvas.drawAxis = false;
			canvas.drawBoundingBox = true;
		}
		//scale vfTh
		canvas.setVFThreshold((float)Math.sqrt(3) * voxelSize * vfTh);
		canvas.setPointVF(x, y, z, dx, dy, dz, color, color_vf, Canvas3DPt.POINT_VF);
	}
	
	public void removeVF(){
		if (canvas == null){
			canvas = new Canvas3DPt();
			canvas.drawAxis = false;
			canvas.drawBoundingBox = true;
		}
		canvas.setArray(x,y,z,voxelSize*0.5f,color,Canvas3DPt.POINT);
	}
	
	void initMultiBandRGB(ImageObject imObj, boolean r, boolean g, boolean b) {
		width = imObj.getNumCols();
		height = imObj.getNumRows();

		volumeSize = width * height * depth;

		maxAxisSize = (float)Math.max(width,height);
		voxelSize = 1 / maxAxisSize;

		float val;

		validVoxelNum = 0;
		for(int k = 0; k<depth; k++) {
			validVoxelNum = getValidPixelNum(imObj,k);
		}

		x = new float[validVoxelNum];
		y = new float[validVoxelNum];
		z = new float[validVoxelNum];
		color = new Color4f[validVoxelNum];

		int cnt = 0;
		float vr,vg,vb;
		for(int k = 0; k<depth; k++) {
			for(int j = 0; j<height; j++) {
				for(int i = 0; i<width; i++) {
					val = imObj.getInt(j,i,k);
//					val = (float)ImageTool.getValueInt(imObj,i,j,k+1);
					if(val < this.voxelLowerThresold || val > this.voxelUpperThresold) continue;

					x[cnt] = voxelSize * (float)i - 0.5f;
					y[cnt] = voxelSize * (float)j - 0.5f;
					z[cnt] = -1*voxelSize * (float)k * gap;

//					color[cnt++] = new Color3f(valR/512f,valG/512f,valB/512f);
					vr = r?val/256f:0f;
					vg = g?val/256f:0f;
					vb = b?val/256f:0f;
					color[cnt++] = new Color4f(vr,vg,vb,0f);
				}

			}
		}

	}

	void initMultiBand(ImageObject imObj, boolean pseudo) {
		width = imObj.getNumCols();
		height = imObj.getNumRows();

		volumeSize = width * height * depth;

		maxAxisSize = (float)Math.max(width,height);
		voxelSize = 1 / maxAxisSize;

		float val;
		validVoxelNum = 0;
		for(int k = 0; k<depth; k++) {
			validVoxelNum += getValidPixelNum(imObj,k);
		}

		x = new float[validVoxelNum];
		y = new float[validVoxelNum];
		z = new float[validVoxelNum];
		color = new Color4f[validVoxelNum];

		ColorBar cb = new ColorBar(cMin, cMax);


		int cnt = 0;
		for(int k = 0; k<depth; k++) {
			for(int j = 0; j<height; j++) {
				for(int i = 0; i<width; i++) {
					val = (float)imObj.getInt(j,i,k);
					if(val < this.voxelLowerThresold || val > this.voxelUpperThresold) continue;

					x[cnt] = voxelSize * (float)i - 0.5f;
					y[cnt] = voxelSize * (float)j - 0.5f;
					z[cnt] = voxelSize * ((float)k);// - 0.5f; 

					if(pseudo) {
						color[cnt] = new Color4f(cb.getColor(val));//new Color4f(valR/255f,valG/255f,valB/255f,1);
					}
					else {
						color[cnt] = new Color4f(val/255f,val/255f,val/255f,1);
					}
					cnt++;
				}
			}
		}

	}



	void initOneBand(ImageObject imObj, boolean map, boolean pseudo) {
		width = imObj.getNumCols();
		height = imObj.getNumRows();
		depth = 1;

		volumeSize = width * height * depth;

		maxAxisSize = (float)Math.max(width,height);
		voxelSize = 1 / maxAxisSize;

		float val;
		validVoxelNum = getValidPixelNum(imObj,0);

		x = new float[validVoxelNum];
		y = new float[validVoxelNum];
		z = new float[validVoxelNum];
		color = new Color4f[validVoxelNum];

		ColorBar cb = new ColorBar(cMin, cMax);


		int cnt = 0;
		for(int j = 0; j<height; j++) {
			for(int i = 0; i<width; i++) {
				val = (float)imObj.getInt(j,i,0);
				if(val < this.voxelLowerThresold || val > this.voxelUpperThresold) continue;
				if(map) {
					x[cnt] = voxelSize * (float)i - 0.5f;
					y[cnt] = voxelSize * (float)j - 0.5f;
					z[cnt] = voxelSize * ((float)val);
				} 
				else {
					x[cnt] = voxelSize * (float)i - 0.5f;
					y[cnt] = voxelSize * (float)j - 0.5f;
					z[cnt] = voxelSize * ((float)0);// - 0.5f; 
				}

				if(pseudo) {
					color[cnt] = new Color4f(cb.getColor(val));//new Color4f(valR/255f,valG/255f,valB/255f,1);
				}
				else {
					color[cnt] = new Color4f(val/255f,val/255f,val/255f,1);
				}

				cnt++;
			}
		}
	}



	void init3Band(ImageObject imObj, boolean map, int evalBand) {
		width = imObj.getNumCols();
		height = imObj.getNumRows();
		depth = 1;
		volumeSize = width * height * depth;

		maxAxisSize = (float)Math.max(width,height);
		voxelSize = 1 / maxAxisSize;

		float val;
		validVoxelNum = 0;
		for(int j = 0; j<height; j++) {
			for(int i = 0; i<width; i++) {
				val = imObj.getInt(j,i,evalBand);
//				val = (float)ImageTool.getValueInt(imObj,i,j,k+1);
				if(val >= this.voxelLowerThresold && val <= this.voxelUpperThresold) {
					validVoxelNum++;
				}

			}
		}

		x = new float[validVoxelNum];
		y = new float[validVoxelNum];
		z = new float[validVoxelNum];
		color = new Color4f[validVoxelNum];

//		ColorBar cb = new ColorBar(cMin, cMax);

		int cnt = 0;
		for(int j = 0; j<height; j++) {
			for(int i = 0; i<width; i++) {
				val = (float)imObj.getInt(j,i,evalBand);
				//	 	 	     val = (float)ImageTool.getValueInt(imObj[k],i,j,_band);
				if(val < this.voxelLowerThresold || val > this.voxelUpperThresold) continue;


				if(map) {
					x[cnt] = voxelSize * (float)i - 0.5f;
					y[cnt] = voxelSize * (float)j - 0.5f;
					z[cnt] = voxelSize * ((float)val);
				} 
				else {
					x[cnt] = voxelSize * (float)i - 0.5f;
					y[cnt] = voxelSize * (float)j - 0.5f;
					z[cnt] = voxelSize * ((float)0);// - 0.5f; 
				}

				color[cnt] = new Color4f(imObj.getFloat(j,i,0)/255f,
						imObj.getFloat(j,i,1)/255f,
						imObj.getFloat(j,i,2)/255f,1);

				cnt++;
			}
		}
	}

	void initMultiFiles(ImageObject[] imObj, boolean pseudo) {

		width = imObj[0].getNumCols();
		height = imObj[0].getNumRows();

		volumeSize = width * height * depth;

		maxAxisSize = (float)Math.max(width,height);
		voxelSize = 1 / maxAxisSize;

		float valR=0, valG=0, valB=0, val=0;
		validVoxelNum = 0;
		for(int k = 0; k<depth; k++) {
			for(int j = 0; j<height; j++) {
				for(int i = 0; i<width; i++) {
					try{
						val = (float) imObj[k].getInt(j, i, _band);
//						val = (float) ImageTool.getValueInt(imObj[k], i, j, _band);
					}
					catch(Exception e) {
						e.printStackTrace();
						System.out.println(k);
						//   new ImageFrame(imObj[k], null, new Point(0,0),"");
					}
					if(val >= this.voxelLowerThresold && val <= this.voxelUpperThresold) {
						validVoxelNum++;
					}

				}
			}
		}

		//if (validVoxelNum < 1) add a black voxel in the origin 
		if (validVoxelNum==0){
			x = y = z = new float[1];
			x[0] = y[0] = z[0] = 0;
			color = new Color4f[1];
			color[0] = new Color4f(0,0,0,1);
			validVoxelNum = 1;
			return;
		}

		x = new float[validVoxelNum];
		y = new float[validVoxelNum];
		z = new float[validVoxelNum];
		x_index = new int[validVoxelNum];
		y_index = new int[validVoxelNum];
		z_index = new int[validVoxelNum];

		if(pseudo) {
			ColorBar cb = new ColorBar(cMin, cMax);
			color = new Color4f[validVoxelNum];

			int cnt = 0;
			for(int k = 0; k<depth; k++) {
				for(int j = 0; j<height; j++) {
					for(int i = 0; i<width; i++) {
						val = (float)imObj[k].getInt(j, i,_band);
//						val = (float)ImageTool.getValueInt(imObj[k],i,j,_band);
						if(val < this.voxelLowerThresold || val > this.voxelUpperThresold) continue;


						x[cnt] = voxelSize * (float)i - 0.5f;
						y[cnt] = voxelSize * (float)j - 0.5f;
						z[cnt] = voxelSize * ((float)k);// - 0.5f;
						x_index[cnt] = i;
						y_index[cnt] = j;
						z_index[cnt] = k;

						color[cnt++] = new Color4f(cb.getColor(val));//new Color4f(valR/255f,valG/255f,valB/255f,1);
					}
				}
			}

		}
		else {
			color = new Color4f[validVoxelNum];

			int cnt = 0;
			for(int k = 0; k<depth; k++) {
				for(int j = 0; j<height; j++) {
					for(int i = 0; i<width; i++) {
						val = imObj[k].getInt(j,i,_band);
						//val = (float)ImageTool.getValueInt(imObj[k],i,j,_band);
						if(val < this.voxelLowerThresold || val > this.voxelUpperThresold) 
							continue;
						if(imObj[k].getNumBands() == 1) {
							valR = valG = valB = imObj[k].getInt(j,i,_band);
						}
						else {
							valR = imObj[k].getInt(j,i,0);
							valG = imObj[k].getInt(j,i,1);
							valB = imObj[k].getInt(j,i,2);
						}

						x[cnt] = voxelSize * (float)i - 0.5f;
						y[cnt] = voxelSize * (float)j - 0.5f;
						z[cnt] = voxelSize * ((float)k);// - 0.5f;
						x_index[cnt] = i;
						y_index[cnt] = j;
						z_index[cnt] = k;

						color[cnt++] = new Color4f(valR/255f,valG/255f,valB/255f,1);

					}

				}
			}
		}
		System.gc();
	}

	void arrayPack(Object[] arr) {
		int cnt;
		for(cnt=0; cnt<arr.length; cnt++) {
			if(arr[cnt] == null)
				break;
		}

		Object[] newArr = new Object[cnt];
		for(int i=0; i<cnt; i++)
			newArr[i] = arr[i];

		arr = newArr;
	}

	public boolean isMasked(int x, int y, int z, ImageObject[] maskObj){
		float val = (float) maskObj[z].getInt(y, x, _band);
		if(val < this.voxelLowerThresold || val > this.voxelUpperThresold)
			return true;
		return false;
	}

	public void mask(ImageObject[] imObj){
		//count valid voxels in the mask and keep track of indices
		int unmaskedCnt = 0;
		int[] tempIndex = new int[validVoxelNum];
		for (int i=0;i<validVoxelNum;i++){
			if (!isMasked(x_index[i], y_index[i], z_index[i], imObj)){
				tempIndex[unmaskedCnt++] = i;
			}
		}
		float[] newX, newY, newZ;
		int[] newXindex, newYindex, newZindex;
		Color4f[] newColor = new Color4f[unmaskedCnt];
		newX = new float[unmaskedCnt];
		newY = new float[unmaskedCnt];
		newZ = new float[unmaskedCnt];
		newXindex = new int[unmaskedCnt];
		newYindex = new int[unmaskedCnt];
		newZindex = new int[unmaskedCnt];

		for (int i=0;i<unmaskedCnt;i++){
			newX[i] = x[tempIndex[i]];
			newY[i] = y[tempIndex[i]];
			newZ[i] = z[tempIndex[i]];
			newColor[i] = color[tempIndex[i]];
			newXindex[i] = x_index[tempIndex[i]];
			newYindex[i] = y_index[tempIndex[i]];
			newZindex[i] = z_index[tempIndex[i]];
		}
		
		validVoxelNum = unmaskedCnt;
		
		x = newX;
		y = newY;
		z = newZ;
		x_index = newXindex;
		y_index = newYindex;
		z_index = newZindex;
		color = newColor;
		
		if (canvas.dx!=null && canvas.dy!=null && canvas.dz!=null && color_vf!=null){
			float[] newdx = new float[unmaskedCnt];
			float[] newdy = new float[unmaskedCnt];
			float[] newdz = new float[unmaskedCnt];
			Color4f[] newColorVF = new Color4f[unmaskedCnt];
	
			for (int i=0;i<unmaskedCnt;i++){
				newdx[i] = dx[tempIndex[i]];
				newdy[i] = dy[tempIndex[i]];
				newdz[i] = dz[tempIndex[i]];
				newColorVF[i] = color_vf[tempIndex[i]];
			}

			dx = newdx;
			dy = newdy;
			dz = newdz;
			color_vf = newColorVF;
			
		}else{
			System.err.println("ERROR: at least one vector field data structure is empty!");
		}
		
		System.out.println(validVoxelNum + " valid voxels after masking.");

		canvas.setArray(x,y,z,voxelSize*0.5f,color,Canvas3DPt.POINT);
		
	}
	
	public void setBoundingBox(int[] newBB){
		boundingBox = newBB;
	}
	
	public void setBoundingBox(int minX, int maxX, int minY, int maxY, int minZ, int maxZ, ImageObject[] maskObj){
		int visibleVoxelNum = 0;
		int[] tempIndex = new int[validVoxelNum];
		for (int i=0;i<validVoxelNum;i++){
			if (x_index[i]>=minX && x_index[i]<=maxX &&
					y_index[i]>=minY && y_index[i]<=maxY &&
					z_index[i]>=minZ && z_index[i]<=maxZ){
				if (maskObj!=null){
					if (!isMasked(x_index[i], y_index[i], z_index[i], maskObj))
						tempIndex[visibleVoxelNum++] = i;
				}else{
					tempIndex[visibleVoxelNum++] = i;
				}
			}
		}

//		System.out.println("visibleVoxelNum = " + visibleVoxelNum);

		int[] index = new int[visibleVoxelNum];
		for (int i=0;i<visibleVoxelNum;i++){
			index[i] = tempIndex[i];
		}
		
		viewSelectedVoxels(index);
		
	}
	
	
	private void drawEmptyPanel(){
		float[] newX = new float[1];
		float[] newY = new float[1];
		float[] newZ = new float[1];
		Color4f[] newColor = new Color4f[1];
		newX[0] = newY[0] = newZ[0] = 0;
		newColor[0] = new Color4f(0f, 0f, 0f, 0f);
		canvas = new Canvas3DPt();
		canvas.setArray(newX, newY, newZ, voxelSize*0.5f, newColor, Canvas3DPt.POINT);		
	}
	
	private void viewSelectedVoxels(int[] index){
		
		if (index.length == 0){
			drawEmptyPanel();
			return;
		}
		float[] newX = new float[index.length];
		float[] newY = new float[index.length];
		float[] newZ = new float[index.length];
		Color4f[] newColor = new Color4f[index.length];
		float[] newdx = new float[index.length];
		float[] newdy = new float[index.length];
		float[] newdz = new float[index.length];
		Color4f[] newdColor = new Color4f[index.length];
		
		for (int i=0;i<index.length;i++){
			newX[i] = x[index[i]];
			newY[i] = y[index[i]];
			newZ[i] = z[index[i]];
			newColor[i] = color[index[i]];
			if (dx!=null){
				newdx[i] = dx[index[i]];
				newdy[i] = dy[index[i]];
				newdz[i] = dz[index[i]];
				newdColor[i] = color_vf[index[i]];			
			}
		}
		
		switch (canvas.getType()){
		case Canvas3DPt.POINT:
			canvas.setArray(newX, newY, newZ, voxelSize*0.5f, newColor, Canvas3DPt.POINT);
			break;
		case Canvas3DPt.POINT_VF:
			canvas.setPointVF(newX, newY, newZ, newdx, newdy, newdz, color, color_vf, Canvas3DPt.POINT_VF);
			break;
		}
		
	}

	public Canvas3D getCanvas() {
		return canvas.getCanvas3D();
	}

	public int getValidVoxelNum() {
		return validVoxelNum;
	}


}


