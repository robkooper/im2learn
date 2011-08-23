package edu.illinois.ncsa.isda.imagetools.ext.conversion;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.datatype.SubArea;
import edu.illinois.ncsa.isda.imagetools.core.io.ImageLoader;
import edu.illinois.ncsa.isda.imagetools.ext.conversion.SpFilters;
/*
 * Test_SpFilters.java
 *
  */

/**
 *
 * @author  Peter Bajcsy
 * @version 1.0
 */


public class Test_SpFilters extends Object {

  private SpFilters _mySpFilters = new SpFilters();
  public  boolean _testPassed=true;
  // input image file name.
  // output is the edge image
  public static void main(String args[])throws Exception{

    Test_SpFilters myTest = new Test_SpFilters();

    System.out.println("argument length="+args.length);
    for(int i=0;i<args.length;i++){
       System.out.println("args[" + i + "]:" + args[i]);
    }
    if(args == null || args.length <1){
    	System.out.println("Please, specify the image to analyze and to save output to");
    	return;
    }
    
    String InFileName, OutFileName;
    InFileName = args[0];
    System.out.println(InFileName);

    OutFileName = args[1];
    System.out.println(OutFileName);

    boolean ret = false;
    //ret  = myTest.TestHighPass(InFileName, OutFileName);
    ret  = myTest.TestMedian(InFileName, OutFileName);
    //ret = myTest.TestLowPass(InFileName, OutFileName);
    //ret  = myTest.TestTextureAggregation(InFileName, OutFileName);
    System.out.println("Test Result = " + ret);

  }

  // constructor
  public Test_SpFilters() {
  }


  // load any image and apply low pass operator
  // save out .iip image
  public boolean TestMedian(String InFileName, String OutFileName)throws Exception{

    ImageObject testObject= null;
    testObject  = ImageLoader.readImage(InFileName);
    System.out.println("Info: this is the input image filename="+InFileName);
    testObject.toString();
    ////////////////


    ImageObject retObject= null;

    _mySpFilters.setKernel(15,15);
    _mySpFilters.toString();
    _mySpFilters.setImage(testObject);
    
    _mySpFilters.filter(_mySpFilters.MEDIAN);//.LowPassOut(testObject);

    retObject = _mySpFilters.getResult();
    
    if(retObject == null){
       System.out.println("ERROR: could not lowpass ");
       return false;
    }
    ImageLoader.writeImage(OutFileName,retObject);

    return true;

  }
  
  public boolean TestLowPass(String InFileName, String OutFileName)throws Exception{

	    ImageObject testObject= null;
	    testObject  = ImageLoader.readImage(InFileName);
	    System.out.println("Info: this is the input image filename="+InFileName);
	    testObject.toString();
	    ////////////////


	    ImageObject retObject= null;

	    _mySpFilters.setKernel(15,15);
	    _mySpFilters.toString();
	    _mySpFilters.setImage(testObject);
	    
	    _mySpFilters.filter(_mySpFilters.LOW_PASS);//.LowPassOut(testObject);

	    retObject = _mySpFilters.getResult();
	    
	    if(retObject == null){
	       System.out.println("ERROR: could not lowpass ");
	       return false;
	    }
	    ImageLoader.writeImage(OutFileName,retObject);

	    return true;

	  }

  public boolean TestHighPass(String InFileName, String OutFileName)throws Exception{

	    ImageObject testObject= null;
	    testObject  = ImageLoader.readImage(InFileName);
	    System.out.println("Info: this is the input image filename="+InFileName);
	    testObject.toString();
	    ////////////////


	    ImageObject retObject= null;

	    _mySpFilters.setKernel(5,5);
	    _mySpFilters.toString();
	    _mySpFilters.setImage(testObject);
	    
	    _mySpFilters.filter(_mySpFilters.HIGH_PASS);

	    retObject = _mySpFilters.getResult();
	    
	    if(retObject == null){
	       System.out.println("ERROR: could not lowpass ");
	       return false;
	    }
	    ImageLoader.writeImage(OutFileName,retObject);

	    return true;

  }
  /*
  // experimental code
  public boolean TestTextureAggregation(String InFileName, String OutFileName)throws Exception{

    //TIFFImage myTiff = new TIFFImage();
    ImInout myio = new ImInout();
    SubArea area = new SubArea(0,0,10,20,false);

    boolean ret=true;
    ret = myio.ImLoad(InFileName,area);
    System.out.println(ret);
    if(!ret){
        System.out.println("ERROR: could not read the input image");
        return false;
    }

    ImageObject testObject= null;
    testObject  = myio.GetImageObject();
    System.out.println("Info: this is the input image");
    testObject.PrintImageObject();

    ImageObject retObject= null;

    _mySpFilters.SetKernel(3,3);
    _mySpFilters.PrintSpFilters();
    retObject = _mySpFilters.AggregateOut(testObject);

    if(retObject == null){
       System.out.println("ERROR: could not aggregate ");
       return false;
    }

    ImCalculator myCalc = new ImCalculator();
    if( !myCalc.Subtract(testObject,retObject) ){
      System.err.print("ERROR: could not compute subtraction of two images");
      return false;
    }
    ImStats myStats = new ImStats();

    if( !myStats.MinMaxVal(myCalc.GetResultIm() ) ){
      System.err.print("ERROR: could not compute min and max val of image difference");
      return false;
    }
    int i;
    for( i=0;i<myStats.GetMinVal().sampPerPixel;i++){
      System.out.println("TEST: i="+i+",min of difference = "+myStats.GetMinVal().imageInt[i] +", max of difference = "+myStats.GetMaxVal().imageInt[i] );
    }
    Histogram hist = new Histogram();
    hist.Hist(myCalc.GetResultIm(), 0);
    int sum = 0;
    for(i=1;i<hist.GetNumBins();i++){
      sum += hist._HistData[i];
    }
    System.out.println("TEST: num of changed pixels= "+sum );

    //IIPImage myIIP = new IIPImage();
    //ret = myIIP.Write(OutFileName,retObject);
    TIFFImage myTiff = new TIFFImage();
    ret = myTiff.Write(OutFileName,retObject);
    System.out.println(ret);

    return(ret);

  }
*/

}