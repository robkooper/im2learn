package edu.illinois.ncsa.isda.im2learn.ext.edge;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageLoader;
import edu.illinois.ncsa.isda.im2learn.ext.edge.Edge;

/**
 * 
 * @author Peter Bajcsy
 * @version 1.0
 */

public class Test_Edge extends Object {

	public boolean		testPassed	= true;

	private static Log	logger		= LogFactory.getLog(Test_Edge.class);

	// input image file name.
	// output is the edge image
	public static void main(String args[]) throws Exception {

		Test_Edge myTest = new Test_Edge();

		System.out.println("argument length=" + args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.println("args[" + i + "]:" + args[i]);
		}
		if ((args == null) || (args.length < 1)) {
			System.out.println("Please, specify the image to analyze and to save output to");
			System.out.println("arg = Input_ImageName, Output_ImageName");
			return;
		}

		String InFileName1, OutFileName;

		InFileName1 = args[0];
		System.out.println(InFileName1);

		OutFileName = args[1];
		System.out.println(OutFileName);

		// read the input images 
		ImageObject testObject1 = null;
		testObject1 = ImageLoader.readImage(InFileName1);
		System.out.println("Info: this is the input image filename=" + InFileName1);
		testObject1.toString();
		////////////////

		// perform calculations
		boolean ret = true;
		ImageObject retObject = null;
		ImageObject retObject1 = null;

		Edge myEdge = new Edge();
		if (!myEdge.RobertsVar(testObject1)) {
			//if (!myEdge.RobertsVar(testObject1)) {
			//if (!myEdge.SobelMag(testObject1)) {
//				if (!myEdge.Sobel(testObject1)) {
			logger.debug("Sobel calculation did not pass");
			ret = false;
		}
		retObject = myEdge.GetEdgeImageObject();
		/*
		 * int maxPts = 10000; float thresh = myEdge.FindThreshEdge(retObject,
		 * maxPts); ImageObject edgeList = myEdge.CreateEdgeList(retObject,
		 * maxPts); retObject1 = myEdge.EdgeList2Image(edgeList,
		 * retObject.getNumRows(), retObject.getNumCols());
		 */
		retObject1 = myEdge.EdgeForLocalDifference(testObject1);
		// save out the retObject
		ImageLoader.writeImage(OutFileName, retObject1);

		System.out.println("Test Result = " + ret);

	}

	// constructor
	public Test_Edge() {
		// TBD
	}

}