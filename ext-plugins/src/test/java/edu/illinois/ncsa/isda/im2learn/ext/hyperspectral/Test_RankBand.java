package edu.illinois.ncsa.isda.im2learn.ext.hyperspectral;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.datatype.SubArea;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageLoader;
import edu.illinois.ncsa.isda.im2learn.ext.hyperspectral.RankBand;

/**
 *
 * @author  Peter Bajcsy
 * @version 1.0
 */






public class Test_RankBand extends Object {

  private RankBand _myRankBand = new RankBand();
  public  boolean _testPassed=true;
  public int _testNumber = 1;
  // input image file name.
  // output is the file with ordered bands
  public static void main(String args[])throws Exception{

    Test_RankBand myTest = new Test_RankBand();

    System.out.println("argument length="+args.length);
    for(int i=0;i<args.length;i++){
       System.out.println("args[" + i + "]:" + args[i]);
    }

    String InFileName1, InFileName2, OutFileName;
    if(args == null || args.length <1){
    	System.out.println("Please, specify the image to analyze");
    	return;
    }
    InFileName1 = args[0];
    System.out.println(InFileName1);

    InFileName2 = args[1];
    System.out.println(InFileName2);

    //OutFileName = args[2];
    //System.out.println(OutFileName);

    boolean ret = false;

    myTest.SetTestNumber(1);
    ret = myTest.TestMethods(InFileName1);

    System.out.println("Test Result = " + ret);

  }

  // constructor
  public Test_RankBand() {
  }
  public boolean SetTestNumber(int val){
     if(val < 0){
       System.out.println("ERROR: test number is invalid");
       return false;
     }
     _testNumber = val;
     return true;
  }
  public int GetTestNumber(){ return _testNumber;}

  public boolean TestMethods(String InFileName1)throws Exception{
    //ImLoader myio1 = new ImInout();
    SubArea area = new SubArea(0,0,10,20);

    
    ImageObject testObject1= null;
    testObject1  = ImageLoader.readImage(InFileName1);
    System.out.println("Info: this is the input image filename="+InFileName1);
    testObject1.toString();
    ////////////////

    switch(_testNumber){
      case 0:
        if( !_myRankBand.RankBasedEntropy(testObject1) ){
            System.out.println("ERROR: could not compute the entropy measure of the input images");
            return false;
        }
        break;
      case 1:
        if( !_myRankBand.RankBasedSharpness(testObject1) ){
            System.out.println("ERROR: could not compute the sharpness measure of the input images");
            return false;
        }
        break;
      case 2:
        if( !_myRankBand.RankBased1stDeriv(testObject1) ){
            System.out.println("ERROR: could not compute the 1st deriv. measure of the input images");
            return false;
        }
        break;
      case 3:
        if( !_myRankBand.RankBased2ndDeriv(testObject1) ){
            System.out.println("ERROR: could not compute the 2nd deriv. measure of the input images");
            return false;
        }
        break;

      default:
         System.out.println("ERROR: test method is out of bounds");
         break;
    }
    if(_myRankBand.GetOrderBands() == null || _myRankBand.GetScoreBands()==null){
       System.out.println("ERROR: result order or score array is empty ");
       return false;
    }

    int idx;
    for(idx=0;idx<testObject1.getNumBands();idx++){
      System.out.println("Idx="+idx+", rank="+_myRankBand.GetOrderBands(idx)+", score="+_myRankBand.GetScoreBands(idx) );
    }

    return true;

  }


}