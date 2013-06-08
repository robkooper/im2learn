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

import com.sun.j3d.utils.geometry.*;
import com.sun.j3d.utils.universe.*;
import javax.media.j3d.*;
import javax.vecmath.*;
//import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

import com.sun.j3d.utils.behaviors.vp.*;


public class Canvas3DPt {

	private SimpleUniverse univ;
	private OrbitBehavior orbit;
	private BoundingSphere bounds;
	private BranchGroup scene;
	private ViewingPlatform viewingPlatform;
	private BranchGroup objRoot;
	private TransformGroup objTrans;
	private TransparencyInterpolator traInt;

	Canvas3D canvas;

	public static final int SPHERE = 0, VOXEL = 1;
	public static final int POINT = 2, LINE = 3, MCUBE = 4, POINT_VF = 5;

	private int type = Canvas3DPt.POINT;//Canvas3DPt.MCUBE;

	float[][] x, y, z;
	float[][] dx, dy, dz;
	float[][] dx0, dy0, dz0; //vectors begin
	float[][] dx1, dy1, dz1; //vectors end
	float size = 0.1f;
	Color4f[][] color, color_vf;
	float axisStroke = 0.005f;
	float axisLength = 3f;
	
	float vfTh = 1.0f;

	int width=500, height=500;

	public boolean drawAxis = false;
	public boolean drawBoundingBox = false;

	public float voxelSize = 0.01f;
	//   public voxelObject[] sArray;


	//public void Canvas3DPt() {}

	public Canvas3DPt() {
		// sArray = null;
		canvas = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
		univ = new SimpleUniverse(canvas);
		canvas.setSize(width,height);
		canvas.setDoubleBufferEnable(true);

		orbit = new OrbitBehavior(canvas);
		bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 10.0);
		orbit.setSchedulingBounds(bounds);

		viewingPlatform = univ.getViewingPlatform(); // add mouse behaviors to the ViewingPlatform
		univ.getViewingPlatform().setNominalViewingTransform();
		viewingPlatform.setViewPlatformBehavior(orbit);

		scene = setBackground();
		scene.setCapability(Group.ALLOW_CHILDREN_EXTEND);
		scene.setCapability(Group.ALLOW_CHILDREN_READ);
		scene.setCapability(Group.ALLOW_CHILDREN_WRITE);

	}

	public Canvas3DPt(int i) {
		// sArray = new voxelObject[i];
	}


	public void setWinSize(int w, int h) {
		width = w;
		height = h;
	}
	
	public void setType(int t){
		type = t;
	}
	
	public int getType(){
		return type;
	}
	
	public void setVFThreshold(float th){
		vfTh = th;
	}

	public void setVoxelSize(float s){
		if (s<=0){
			System.out.println("WARNING: voxelSize should be greater than 0!!!");
		}else
			voxelSize = s;
	}

	public void setArray(float[] x, float[] y, float[] z, float size, Color4f[] color, int type) {
		int len = x.length;
		this.type = type;
		this.x = new float[1][];
		this.y = new float[1][];
		this.z = new float[1][];
		this.color = new Color4f[1][];

		this.x[0] = x;
		this.y[0] = y;
		this.z[0] = z;
		this.color[0] = color;

		this.size = 0.01f;

		redraw();
	}

	public void setArray(float[][] x, float[][] y, float[][] z, float size, Color4f[][] color) {
		int len = x.length;
		this.type = this.LINE;
		this.x = new float[1][];
		this.y = new float[1][];
		this.z = new float[1][];
		//   this.size = size;
		//  this.size = 0.01f;

		this.x = x;
		this.y = y;
		this.z = z;

		this.color = color;
		redraw();
	}

	public void addArray(float[] x, float[] y, float[] z, float size, Color4f[] color, int type) {
		int len = x.length;
		this.type = type;
		this.x = new float[1][];
		this.y = new float[1][];
		this.z = new float[1][];
		this.color = new Color4f[1][];

		this.x[0] = x;
		this.y[0] = y;
		this.z[0] = z;
		this.color[0] = color;

		this.size = 0.01f;

		scene.addChild(createObjectGroup());
	}
	
	public void setPointVF(float[] x, float[] y, float[] z, float[] dx, float[] dy, float[] dz, Color4f[] color, Color4f[] color_vf, int type){
		this.type = POINT_VF;

		this.x = new float[1][];
		this.y = new float[1][];
		this.z = new float[1][];
		this.dx = new float[1][];
		this.dy = new float[1][];
		this.dz = new float[1][];
		this.color = new Color4f[1][];
		this.color_vf = new Color4f[1][];
		
		this.x[0] = x;
		this.y[0] = y;
		this.z[0] = z;
		this.dx[0] = dx;
		this.dy[0] = dy;
		this.dz[0] = dz;
		this.color[0] = color;
		this.color_vf[0] = color_vf;
		
		this.size = 0.01f;
		
		redraw();
	}
	
	public void addVectorArray(float[] x, float[] y, float[] z, float[] dx, float[] dy, float[] dz, Color4f[] color, int type){
		this.type = POINT;

		this.x = new float[1][];
		this.y = new float[1][];
		this.z = new float[1][];
		this.dx = new float[1][];
		this.dy = new float[1][];
		this.dz = new float[1][];
		this.color = new Color4f[1][];
		
		this.x[0] = x;
		this.y[0] = y;
		this.z[0] = z;
		this.dx[0] = dx;
		this.dy[0] = dy;
		this.dz[0] = dz;
		this.color[0] = color;
		
		this.size = 0.01f;
		
		scene.addChild( createObjectGroup() ); 
	}
	
	public void init() {

		canvas = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
		univ = new SimpleUniverse(canvas);
		canvas.setSize(width,height);
		canvas.setDoubleBufferEnable(true);

		orbit = new OrbitBehavior(canvas);
		bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 10.0);
		orbit.setSchedulingBounds(bounds);

		viewingPlatform = univ.getViewingPlatform(); // add mouse behaviors to the ViewingPlatform
		univ.getViewingPlatform().setNominalViewingTransform();
		viewingPlatform.setViewPlatformBehavior(orbit);

		scene = setBackground();
		scene.setCapability(Group.ALLOW_CHILDREN_EXTEND);
		scene.setCapability(Group.ALLOW_CHILDREN_READ);
		scene.setCapability(Group.ALLOW_CHILDREN_WRITE);
		
		redraw();
		
	}
	
	//draw the data in the scene
	public void redraw(){
		//remove all detachable BranchGroups 
		int i=0;
		while(i<scene.numChildren()){
			Node bg = scene.getChild(i); 
			if ( bg instanceof BranchGroup && bg.getCapability(BranchGroup.ALLOW_DETACH)){
				scene.removeChild(i);
			}else
				i++;
		}
		
		//redraw the whole scene
		if(drawAxis) {
			scene.addChild(createAxis());
		}

		if(drawBoundingBox) {
			scene.addChild(addBoundingBox(new Color3f(Color.white)));
		}

		scene.addChild(createObjectGroup());
	}

//	public void update(){
//		//remove all detachable BranchGroups 
//		int i=0;
//		while(i<scene.numChildren()){
//			Node bg = scene.getChild(i); 
//			if ( bg instanceof BranchGroup && bg.getCapability(BranchGroup.ALLOW_DETACH)){
//				scene.removeChild(i);
//			}else
//				i++;
//		}
//		//redraw the whole scene
//		draw();
//	}

	public void compile() {
		scene.compile();
		univ.addBranchGraph(scene);
	}

	public BranchGroup setBackground() {
		objRoot = new BranchGroup();
		
		BoundingSphere bounds =
			new BoundingSphere(new Point3d(0.0,0.0,0.0), 10.0F);

		// Set up the background
		Color3f bgColor = new Color3f(0f, 0f, 0f);
		Background bgNode = new Background(bgColor);
		bgNode.setApplicationBounds(bounds);

		// Set up the ambient light
		Color3f ambientColor = new Color3f(1f, 1f, 1f);
		AmbientLight ambientLightNode = new AmbientLight(ambientColor);
		ambientLightNode.setInfluencingBounds(bounds);

		// Set up the directional lights
		Color3f light1Color = new Color3f(1.0f, 0f, 1.0f);
		Vector3f light1Direction  = new Vector3f(10.0f, 10.0f, 10.0f);
		Color3f light2Color = new Color3f(1.0f, 1.0f, 1.0f);
		Vector3f light2Direction  = new Vector3f(-10.0f, -10.0f, -10.0f);

		DirectionalLight light1
		= new DirectionalLight(light1Color, light1Direction);
		light1.setInfluencingBounds(bounds);

		DirectionalLight light2
		= new DirectionalLight(light2Color, light2Direction);
		light2.setInfluencingBounds(bounds);


		objRoot.addChild(bgNode);
		objRoot.addChild(ambientLightNode);
		objRoot.addChild(light1);
		objRoot.addChild(light2);
		//  objRoot.addChild(traInt);

		return objRoot;
	}



	public BranchGroup createObjectGroup() {

		BranchGroup objGrp = new BranchGroup();
		
		objGrp.setCapability(BranchGroup.ALLOW_DETACH);

		//    int numRow = 3, numCol = 5;

		//    float colorInc = 1/(float)(numRow*numCol), rowInc = 1/(float)numRow, colInc = 1/(float)numCol;
		//    float color=0, row=0, col=0;

		if (type == Canvas3DPt.POINT) {

//			TransparencyAttributes objTransp = new TransparencyAttributes(); 
//			objTransp.setCapability(TransparencyAttributes.ALLOW_VALUE_WRITE); 
//			objTransp.setTransparencyMode(TransparencyAttributes.BLENDED);

//			Alpha alpha = new Alpha(-1, 
//			Alpha.INCREASING_ENABLE + Alpha.DECREASING_ENABLE, 
//			0, 0, 2000, 0, 1000, 2000, 0, 1000);

			objGrp.addChild(new PointSet3D(x[0], y[0], z[0], color[0]));

			//	  traInt = createTransparencyInterpolator(alpha, objTransp, bounds);

		}
		else if(type == Canvas3DPt.VOXEL) {
			objGrp.addChild(new VoxelSet3D(x[0], y[0], z[0], color[0], voxelSize));
		}
		else if(type == Canvas3DPt.MCUBE) {
			objGrp.addChild(new MCube(x[0], y[0], z[0], color[0], 10));
			//  objGrp.addChild(this.addSphere(0f,0f,0f,0.4f,new Color3f(Color.WHITE)));
		}
//		else if (type == Canvas3DPt.LINE) {
//		for (int i = 0; i < x.length; i++) {
//		objGrp.addChild(new LineSet3D(x[i], y[i], z[i], color[i]));
//		}
//		}
		//   objGrp.addChild(new CubeSet(size, x, y, z, color));
		else if(type == Canvas3DPt.POINT_VF) {
			objGrp.addChild(new PointSet3D(x[0], y[0], z[0], color[0]));
			objGrp.addChild(new VectorSet3D(x[0], y[0], z[0], dx[0], dy[0], dz[0], color_vf[0]));
		}

		return objGrp;

	}

	private TransparencyInterpolator createTransparencyInterpolator(Alpha alpha, TransparencyAttributes objTransp, BoundingSphere bounds) { 
		TransparencyInterpolator traInt = new TransparencyInterpolator(alpha, objTransp); 
		traInt.setSchedulingBounds(bounds); 
		return traInt; 
	} 

	public TransformGroup addSphere(float x, float y, float z, float size, Color3f c) {

		TransformGroup transgrp = new TransformGroup();
		transgrp.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		transgrp.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);

		Material m = new Material();
		m.setLightingEnable(true);
		m.setDiffuseColor(c);
		Appearance apSphere = new Appearance();
		apSphere.setMaterial(m);


		Transform3D sphereXform = new Transform3D();
		sphereXform.set (new Vector3f(new Point3f(x,y,z)));

		Sphere sphere = new Sphere(size,apSphere);

		transgrp.setTransform(sphereXform);
		transgrp.addChild(sphere);
		return transgrp;
	}


//	public TransformGroup addCube(float x, float y, float z, float size, Color3f c) {

//	TransformGroup transgrp = new TransformGroup();
//	/*   transgrp.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
//	transgrp.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);

//	Material m = new Material();
//	m.setLightingEnable(true);
//	m.setDiffuseColor(c);
//	Appearance apCube = new Appearance();
//	apCube.setMaterial(m);


//	Transform3D cubeXform = new Transform3D();
//	cubeXform.set (new Vector3f(new Point3f(x,y,z)));

//	Box cube = new Box(size,size,size,apCube);
//	//  ColorCube cube = new ColorCube(size);
//	transgrp.setTransform(cubeXform);
//	transgrp.addChild(cube);*/
//	transgrp.addChild(new Cube(size,x,y,z,c));
//	return transgrp;
//	}


	public BranchGroup createAxis() {

		BranchGroup objGrp = new BranchGroup();
		objGrp.setCapability(BranchGroup.ALLOW_DETACH);
		try {
			objGrp.addChild(addAxis(axisLength,axisStroke, 1 ,new Color3f(Color.green))); // x-axis:1, y-axis:2, z-axis:3
			objGrp.addChild(addAxis(axisLength,axisStroke, 2 ,new Color3f(Color.blue))); // x-axis:1, y-axis:2, z-axis:3
			objGrp.addChild(addAxis(axisLength,axisStroke, 3 ,new Color3f(Color.red))); // x-axis:1, y-axis:2, z-axis:3
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		objGrp.setCapability(BranchGroup.ALLOW_DETACH);

		return objGrp;
	}


	public void addText(String text, double x, double y, double z, Color3f c) {
		Transform3D translate = new Transform3D();
		translate.set(new Vector3d(x,y,z));

		TransformGroup transgrp = new TransformGroup(translate);
		transgrp.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		transgrp.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		transgrp.addChild(new Text2D(text,c,"Helvetica", 14, Font.BOLD));

//		scene.addChild(transgrp);
	}


	public TransformGroup addAxis(float length, float stroke, int axis, Color3f c) {

		Transform3D rotate = new Transform3D();

		if(axis == 1)
			rotate.rotY(Math.PI/2);
		else if(axis == 2)
			rotate.rotX(Math.PI/2);
		else if(axis == 3)
			rotate.rotZ(Math.PI/2);

		TransformGroup transgrp = new TransformGroup(rotate);
		transgrp.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		transgrp.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);

		Material m = new Material();
		m.setLightingEnable(true);
		m.setDiffuseColor(c);

		Appearance apAxis = new Appearance();
		apAxis.setMaterial(m);

		transgrp.addChild(new Cylinder(stroke, length, apAxis));

		if (axis == 1)
			transgrp.addChild(new Text2D("  Y", c, "Helvetica", 18, Font.BOLD));
		else if (axis == 2)
			transgrp.addChild(new Text2D("  Z", c, "Helvetica", 18, Font.BOLD));
		else if (axis == 3)
			transgrp.addChild(new Text2D("  X", c, "Helvetica", 18, Font.BOLD));

		return transgrp;
	}

	public BranchGroup addBoundingBox(Color3f c) {
		BranchGroup objGrp = new BranchGroup();
		float[][] box = createBoundingBox(x[0],y[0],z[0]);
		Color4f[] color = new Color4f[box[0].length];
		for(int i=0; i<color.length; i++) {
			color[i] = new Color4f(Color.white);
		}
		objGrp.addChild(new LineSet3D(box[0], box[1], box[2], color));
		
		objGrp.setCapability(BranchGroup.ALLOW_DETACH);

		return objGrp;
	}


	float[][] createBoundingBox(float[] x, float[] y, float[] z) {
		float[] b = getBoundingBox(x,y,z);
		float[][] box = new float[3][];
		box[0] = new float[] {b[0],b[3], b[3],b[3], b[3],b[0], b[0],b[0],
				b[0],b[3], b[3],b[3], b[3],b[0], b[0],b[0],  b[0],b[0],
				b[0],b[0], b[0],b[3], b[3],b[3], b[3],b[3], b[3],b[3], b[3],b[3]};
		box[1] = new float[] {b[1],b[1], b[1],b[4], b[4],b[4], b[4],b[1],
				b[1],b[1], b[1],b[4], b[4],b[4], b[4],b[1], b[1],b[4],
				b[4],b[4], b[4],b[4], b[1],b[1], b[1],b[1], b[1],b[4], b[4],b[4]};
		box[2] = new float[] {b[2],b[2], b[2],b[2], b[2],b[2], b[2],b[2],
				b[5],b[5], b[5],b[5], b[5],b[5], b[5],b[5], b[5],b[5],
				b[5],b[2], b[2],b[2], b[2],b[5], b[5],b[2], b[2],b[2], b[2],b[5]};

		return box;
	}


	float[] getBoundingBox(float[] x, float[] y, float[] z) {
		float[] retLimit = new float[6];

		retLimit[0] = Float.MAX_VALUE;
		retLimit[1] = Float.MAX_VALUE;
		retLimit[2] = Float.MAX_VALUE;

		retLimit[3] = Float.MIN_VALUE;
		retLimit[4] = Float.MIN_VALUE;
		retLimit[5] = Float.MIN_VALUE;


		for(int i=0; i<x.length; i++) {
			retLimit[0] = Math.min(retLimit[0],x[i]);
			retLimit[3] = Math.max(retLimit[3],x[i]);
		}


		for(int i=0; i<y.length; i++) {
			retLimit[1] = Math.min(retLimit[1],y[i]);
			retLimit[4] = Math.max(retLimit[4],y[i]);
		}

		for(int i=0; i<z.length; i++) {
			retLimit[2] = Math.min(retLimit[2],z[i]);
			retLimit[5] = Math.max(retLimit[5],z[i]);
		}

		return retLimit;
	}

	/*
    public TransformGroup addCubes(voxelObject[] cubes) {

      TransformGroup transgrp = new TransformGroup();
      transgrp.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
      transgrp.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);

      for(int i=0; i<cubes.length; i++) {
        Transform3D cubeXform = new Transform3D();
        Material m = new Material();
        m.setLightingEnable(true);
        Appearance apCube = new Appearance();
        m.setDiffuseColor(cubes[i].color);
        apCube.setMaterial(m);
        cubeXform.set (new Vector3f(new Point3f(cubes[i].x,cubes[i].y,cubes[i].z)));

        Box cube = new Box(cubes[i].size,cubes[i].size,cubes[i].size,apCube);
        transgrp.setTransform(cubeXform);
        transgrp.addChild(cube);
      }

      return transgrp;
    }
	 */

	public void destroy() {
		univ.removeAllLocales();
	}

	public Canvas3D getCanvas3D() {
		compile();
		return canvas;
	}

	public class PointSet3D extends Shape3D {

		public PointSet3D(float[] x, float[] y, float[] z, Color4f[] color) {
			PointArray c = new PointArray(x.length, PointArray.COORDINATES | PointArray.COLOR_4 | PointArray.BY_REFERENCE);

			c.setCapability(PointArray.ALLOW_REF_DATA_READ);
			c.setCapability(PointArray.ALLOW_REF_DATA_WRITE);

			//Point3f[] coord = new Point3f[x.length];
			float[] coords = new float[x.length*3];
			float[] colors = new float[color.length*4];

			int cnt = 0, cnt2 = 0;
			for(int i=0; i<x.length; i++) {
				coords[cnt++] = x[i];
				coords[cnt++] = y[i];
				coords[cnt++] = z[i];

				colors[cnt2++] = color[i].x;
				colors[cnt2++] = color[i].y;
				colors[cnt2++] = color[i].z;
				colors[cnt2++] = color[i].w;
			}

//			for(int i=0; i<x.length; i++) {
//	coord[i] = new Point3f(x[i],y[i],z[i]);
//			}

			c.setCoordRefFloat(coords);
			c.setColorRefFloat(colors);

			//    c.setCoordinates(0,coord);
			//    c.setColors(0,color);
			this.setAppearance(createAppearance(1.5f));
			this.setGeometry(c);
		}
		
		Appearance createAppearance(float pointSize) {
			Appearance app = new Appearance();
			PointAttributes pa = new PointAttributes();
			pa.setPointSize(pointSize);
			app.setPointAttributes(pa);
			return app;
		}

		public Shape3D getShape() {
			return this;
		}
	}


	/*
	 * this is a set of lines connecting a set of points
	 */
	public class LineSet3D extends Shape3D {

		public LineSet3D(float[] x, float[] y, float[] z, Color4f[] color) {
			LineArray c = new LineArray((x.length-1)*2, LineArray.COORDINATES | LineArray.COLOR_4);
			Point3f[] coord = new Point3f[(x.length-1)*2];
			Color4f[] cl = new Color4f[(x.length-1)*2];

			for(int i=0; i<x.length-1; i++) {
				coord[i*2] = new Point3f(x[i],y[i],z[i]);
				coord[i*2+1] = new Point3f(x[i+1],y[i+1],z[i+1]);
				cl[i*2] = color[i];
				cl[i*2+1] = color[i+1];
			}

			c.setCoordinates(0,coord);
			c.setColors(0,cl);
			this.setGeometry(c);
		}


		public Shape3D getShape() {
			return this;
		}

	}
	
	/*
	 * this is a set of separate lines
	 */
	public class VectorSet3D extends Shape3D {
		
		public VectorSet3D(float[] x, float[] y, float[] z, float[] dx, float[] dy, float[] dz, Color4f[] color) {
			
			int validVFCount= 0;
			for (int i=0;i<dx.length;i++){
				if (Math.sqrt(dx[i]*dx[i]+dy[i]*dy[i]+dz[i]*dz[i])>vfTh)
					validVFCount++;
			}
			
			System.out.println("vfTh = " + vfTh);
			System.out.println("validVFCount = " + validVFCount);

			LineArray c = new LineArray(validVFCount*2, LineArray.COORDINATES | LineArray.COLOR_4);
			Point3f[] coord = new Point3f[validVFCount*2];
			Color4f[] cl = new Color4f[validVFCount*2];
			
			int cnt = 0;
			for(int i=0; i<dx.length; i++) {
				if (Math.sqrt(dx[i]*dx[i]+dy[i]*dy[i]+dz[i]*dz[i])>vfTh){
					coord[cnt*2] = new Point3f(x[i],y[i],z[i]);
					coord[cnt*2+1] = new Point3f(x[i]+dx[i],y[i]+dy[i],z[i]+dz[i]);
					cl[cnt*2] = cl[cnt*2+1] = color[i];
					cnt++;
				}
			}
			
			/*
			LineArray c = new LineArray((x.length)*2, LineArray.COORDINATES | LineArray.COLOR_4);
			Point3f[] coord = new Point3f[(x.length)*2];
			Color4f[] cl = new Color4f[(x.length)*2];
			
			System.out.println("x.length = " + x.length);
			System.out.println("dx.length = " + dx.length);
			for(int i=0; i<dx.length; i++) {
				coord[i*2] = new Point3f(x[i],y[i],z[i]);
				coord[i*2+1] = new Point3f(x[i]+dx[i],y[i]+dy[i],z[i]+dz[i]);
				cl[i*2] = cl[i*2+1] = color[i];
			}
			*/

			c.setCoordinates(0,coord);
			c.setColors(0,cl);
			this.setGeometry(c);
		}
		
		public Shape3D getShape(){
			return this;
		}
		
	}
	

	/*
	 * this is a set of cubic vexles  
	 */
	public class VoxelSet3D extends Shape3D{
		
		public VoxelSet3D(float[] x, float[] y, float[] z, Color4f[] color, float voxelSize){
			/*
			 * the QuadArray will contain a cube per each voxel
			 * 6 faces per cube, 4 vertex per face : x.length*6*4 verts
			 */
			int numOfVerts = x.length*6*4;
			QuadArray c = new QuadArray(numOfVerts, QuadArray.COORDINATES | QuadArray.COLOR_4 | QuadArray.NORMALS);
			Point3f[] coord = new Point3f[numOfVerts];
			Vector3f[] norm = new Vector3f[numOfVerts];
			Color4f[] cl = new Color4f[numOfVerts];
			float cubeSize = 0.5f*voxelSize;
			int i = 0;
			for (int cnt=0;cnt<numOfVerts;cnt=cnt+24){
				//front face
				coord[cnt] = new Point3f(x[i]-cubeSize,y[i]-cubeSize,z[i]+cubeSize);
				coord[cnt+1] = new Point3f(x[i]+cubeSize,y[i]-cubeSize,z[i]+cubeSize);
				coord[cnt+2] = new Point3f(x[i]+cubeSize,y[i]+cubeSize,z[i]+cubeSize);
				coord[cnt+3] = new Point3f(x[i]-cubeSize,y[i]+cubeSize,z[i]+cubeSize);
				norm[cnt] = norm[cnt+1] = norm[cnt+2] = norm[cnt+3] = new Vector3f(0.0f, 0.0f, 1.0f);
				//back face
				coord[cnt+4] = new Point3f(x[i]-cubeSize,y[i]-cubeSize,z[i]-cubeSize);
				coord[cnt+5] = new Point3f(x[i]+cubeSize,y[i]-cubeSize,z[i]-cubeSize);
				coord[cnt+6] = new Point3f(x[i]+cubeSize,y[i]+cubeSize,z[i]-cubeSize);
				coord[cnt+7] = new Point3f(x[i]-cubeSize,y[i]+cubeSize,z[i]-cubeSize);
				norm[cnt+4] = norm[cnt+5] = norm[cnt+6] = norm[cnt+7] = new Vector3f(0.0f, 0.0f, -1.0f);
				//right face
				coord[cnt+8] = new Point3f(x[i]+cubeSize,y[i]-cubeSize,z[i]-cubeSize);
				coord[cnt+9] = new Point3f(x[i]+cubeSize,y[i]-cubeSize,z[i]+cubeSize);
				coord[cnt+10] = new Point3f(x[i]+cubeSize,y[i]+cubeSize,z[i]+cubeSize);
				coord[cnt+11] = new Point3f(x[i]+cubeSize,y[i]+cubeSize,z[i]-cubeSize);
				norm[cnt+8] = norm[cnt+9] = norm[cnt+10] = norm[cnt+11] = new Vector3f(1.0f, 0.0f, 0.0f);
				//left face
				coord[cnt+12] = new Point3f(x[i]-cubeSize,y[i]-cubeSize,z[i]-cubeSize);
				coord[cnt+13] = new Point3f(x[i]-cubeSize,y[i]-cubeSize,z[i]+cubeSize);
				coord[cnt+14] = new Point3f(x[i]-cubeSize,y[i]+cubeSize,z[i]+cubeSize);
				coord[cnt+15] = new Point3f(x[i]-cubeSize,y[i]+cubeSize,z[i]-cubeSize);
				norm[cnt+12] = norm[cnt+13] = norm[cnt+14] = norm[cnt+15] = new Vector3f(-1.0f, 0.0f, 0.0f);
				//top face
				coord[cnt+16] = new Point3f(x[i]-cubeSize,y[i]+cubeSize,z[i]-cubeSize);
				coord[cnt+17] = new Point3f(x[i]-cubeSize,y[i]+cubeSize,z[i]+cubeSize);
				coord[cnt+18] = new Point3f(x[i]+cubeSize,y[i]+cubeSize,z[i]+cubeSize);
				coord[cnt+19] = new Point3f(x[i]+cubeSize,y[i]+cubeSize,z[i]-cubeSize);
				norm[cnt+16] = norm[cnt+17] = norm[cnt+18] = norm[cnt+19] = new Vector3f(0.0f, 1.0f, 0.0f);
				//bottom face
				coord[cnt+20] = new Point3f(x[i]-cubeSize,y[i]-cubeSize,z[i]-cubeSize);
				coord[cnt+21] = new Point3f(x[i]-cubeSize,y[i]-cubeSize,z[i]+cubeSize);
				coord[cnt+22] = new Point3f(x[i]+cubeSize,y[i]-cubeSize,z[i]+cubeSize);
				coord[cnt+23] = new Point3f(x[i]+cubeSize,y[i]-cubeSize,z[i]-cubeSize);
				norm[cnt+20] = norm[cnt+21] = norm[cnt+22] = norm[cnt+23] = new Vector3f(0.0f, -1.0f, 0.0f);
				for (int j=0;j<24;j++){
					cl[cnt+j] = new Color4f(color[i].x, color[i].y, color[i].z, color[i].w);
				}
				i++;
			}
			
			c.setCoordinates(0, coord);
			c.setNormals(0, norm);
			c.setColors(0, cl);
			
			this.setGeometry(c);
		}
		
		public Shape3D getShape(){
			return this;
		}
		
	}

	public class MCube extends Shape3D {
		ArrayList triangles = new ArrayList();
		ArrayList norms = new ArrayList();
		ArrayList colors = new ArrayList();

		public MCube(float[] x, float[] y, float[] z, Color4f[] color, int samples) {

			float[][][] cubes = new float[samples*2+1][samples*2+1][samples*2+1];

			for(int i=0; i<x.length; i++) {
				cubes[round((x[i]+1)*samples)][round((y[i]+1)*samples)][round((z[i]+1)*samples)] = 1f;
			}


			for(int i=0; i<cubes.length-1; i++) {
				for(int j=0; j<cubes[0].length-1; j++) {
					for(int k=0; k<cubes[0][0].length-1; k++) {
						setTriangle(cubes, i,j,k);
					}
				}
			}


			TriangleArray c = new TriangleArray(triangles.size(),TriangleArray.COORDINATES | 
					TriangleArray.NORMALS |
					TriangleArray.COLOR_4);
			Point3f[] coord = (Point3f[])triangles.toArray();
			Color4f[] cl = (Color4f[])colors.toArray();
			Vector3f[] norm = (Vector3f[])norms.toArray();

//			coord[0] = new Point3f(0f,0.5f,0.4f);
//coord[1] = new Point3f(0.2f,0f,0.4f);
//coord[2] = new Point3f(1f,0.5f,0.4f);
//coord[3] = new Point3f(0f,-0.5f,0f);
//coord[4] = new Point3f(0f,0f,0.4f);
//			coord[5] = new Point3f(0f,0f,0f);

//			cl[0] = new Color4f(Color.WHITE);
//			cl[1] = new Color4f(Color.WHITE);
//			cl[2] = new Color4f(Color.WHITE);
//			cl[3] = new Color4f(Color.WHITE);
//			cl[4] = new Color4f(Color.WHITE);
//			cl[5] = new Color4f(Color.WHITE);
//			norm[0] = getNormal(coord[0],coord[1],coord[2]);
//			norm[1] = getNormal(coord[0],coord[1],coord[2]);
//			norm[2] = getNormal(coord[0],coord[1],coord[2]);
//			norm[3] = getNormal(coord[3],coord[4],coord[5]);
//			norm[4] = getNormal(coord[3],coord[4],coord[5]);
//			norm[5] = getNormal(coord[3],coord[4],coord[5]);
			c.setCoordinates(0,coord);
			c.setColors(0,cl);
			c.setNormals(0,norm);
			this.setAppearance(createAppearance());
			this.setGeometry(c);
		}

		void setTriangle(float[][][] cubes, int i, int j, int k) {
			float[] pts = new float[8];
			pts[0] = cubes[i][j][k];
			pts[1] = cubes[i+1][j][k];
			pts[2] = cubes[i+1][j+1][k];
			pts[3] = cubes[i][j+1][k];
			pts[4] = cubes[i][j][k+1];
			pts[5] = cubes[i+1][j][k+1];
			pts[6] = cubes[i+1][j+1][k+1];
			pts[7] = cubes[i][j+1][k+1];

			// Alan Watt "3D Computer Graphics 3rd ed", pp 383
			//0
			if(pts[0] == 0 &&
					pts[1] == 0 &&
					pts[2] == 0 &&
					pts[3] == 0 &&
					pts[4] == 0 &&
					pts[5] == 0 &&
					pts[6] == 0 &&
					pts[7] == 0) {
				return;
			}
			//1
			else if (pts[0] == 0 &&
					pts[1] == 0 &&
					pts[2] == 0 &&
					pts[3] == 0 &&
					pts[4] == 1 &&
					pts[5] == 0 &&
					pts[6] == 0 &&
					pts[7] == 0) {
			}
			//2
			else if (pts[0] == 0 &&
					pts[1] == 0 &&
					pts[2] == 0 &&
					pts[3] == 0 &&
					pts[4] == 1 &&
					pts[5] == 1 &&
					pts[6] == 0 &&
					pts[7] == 0) {
			}
			//3
			else if (pts[0] == 0 &&
					pts[1] == 1 &&
					pts[2] == 0 &&
					pts[3] == 0 &&
					pts[4] == 1 &&
					pts[5] == 0 &&
					pts[6] == 0 &&
					pts[7] == 0) {
			}
			//4
			else if (pts[0] == 0 &&
					pts[1] == 0 &&
					pts[2] == 1 &&
					pts[3] == 0 &&
					pts[4] == 1 &&
					pts[5] == 0 &&
					pts[6] == 0 &&
					pts[7] == 0) {
			}
			//5
			else if (pts[0] == 0 &&
					pts[1] == 0 &&
					pts[2] == 0 &&
					pts[3] == 0 &&
					pts[4] == 0 &&
					pts[5] == 1 &&
					pts[6] == 1 &&
					pts[7] == 1) {
			}
			//6
			else if (pts[0] == 0 &&
					pts[1] == 0 &&
					pts[2] == 1 &&
					pts[3] == 0 &&
					pts[4] == 1 &&
					pts[5] == 1 &&
					pts[6] == 0 &&
					pts[7] == 0) {
			}
			//7
			else if (pts[0] == 1 &&
					pts[1] == 0 &&
					pts[2] == 1 &&
					pts[3] == 0 &&
					pts[4] == 0 &&
					pts[5] == 1 &&
					pts[6] == 0 &&
					pts[7] == 0) {
			}
			//8
			else if (pts[0] == 0 &&
					pts[1] == 0 &&
					pts[2] == 0 &&
					pts[3] == 0 &&
					pts[4] == 1 &&
					pts[5] == 1 &&
					pts[6] == 1 &&
					pts[7] == 1) {
			}
			//9
			else if (pts[0] == 0 &&
					pts[1] == 0 &&
					pts[2] == 0 &&
					pts[3] == 1 &&
					pts[4] == 1 &&
					pts[5] == 0 &&
					pts[6] == 1 &&
					pts[7] == 1) {
			}
			//10
			else if (pts[0] == 1 &&
					pts[1] == 0 &&
					pts[2] == 1 &&
					pts[3] == 0 &&
					pts[4] == 1 &&
					pts[5] == 0 &&
					pts[6] == 1 &&
					pts[7] == 0) {
			}
			//11
			else if (pts[0] == 0 &&
					pts[1] == 0 &&
					pts[2] == 1 &&
					pts[3] == 0 &&
					pts[4] == 1 &&
					pts[5] == 0 &&
					pts[6] == 1 &&
					pts[7] == 1) {
			}
			//12
			else if (pts[0] == 1 &&
					pts[1] == 0 &&
					pts[2] == 0 &&
					pts[3] == 0 &&
					pts[4] == 0 &&
					pts[5] == 1 &&
					pts[6] == 1 &&
					pts[7] == 1) {
			}
			//13
			else if (pts[0] == 0 &&
					pts[1] == 1 &&
					pts[2] == 0 &&
					pts[3] == 1 &&
					pts[4] == 1 &&
					pts[5] == 0 &&
					pts[6] == 1 &&
					pts[7] == 0) {
			}
			//14
			else if (pts[0] == 0 &&
					pts[1] == 0 &&
					pts[2] == 0 &&
					pts[3] == 1 &&
					pts[4] == 0 &&
					pts[5] == 1 &&
					pts[6] == 1 &&
					pts[7] == 1) {
			}





		}

		public int round(float v) {
			return (int)(v+0.5f);
		}

		public Shape3D getShape() {
			return this;
		}

		private Vector3f getNormal(Point3f coords0, Point3f coords1, Point3f coords2) {
			Vector3f d1 = new Vector3f(coords1.x - coords0.x,
					coords1.y - coords0.y,
					coords1.z - coords0.z);
			Vector3f d2 = new Vector3f(coords2.x - coords1.x,
					coords2.y - coords1.y,
					coords2.z - coords1.z);
			Vector3f cross = new Vector3f(d1.y*d2.z - d1.z*d2.y,
					d1.z*d2.x - d1.x*d2.z,
					d1.x*d2.y - d1.y*d2.x);
			float dist = (float)Math.sqrt(cross.x*cross.x +
					cross.y*cross.y +
					cross.z*cross.z);

			return new Vector3f(cross.x/dist, cross.y/dist, cross.z/dist);

		}


		Appearance createAppearance() {
			Material m = new Material();
			m.setLightingEnable(true);
			Appearance apTri = new Appearance();
			apTri.setMaterial(m);
			return apTri;
		}

	}

}




/*
class voxelObject {
  float x,y,z;
  float size;
  Color3f color;

  voxelObject() {
    x = y = z = 0f;
    size = 0.03f;
    color = new Color3f(1f,0f,0f); // red by default
  }

  voxelObject(float x, float y, float z, float size, Color3f color) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.size = size;
    this.color = color;
  }

  voxelObject(float x, float y, float z, float size, float gray) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.size = size;
    this.color.set(gray,gray,gray);
  }


}


 */
