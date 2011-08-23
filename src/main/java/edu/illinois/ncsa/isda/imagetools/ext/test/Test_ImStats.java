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
package edu.illinois.ncsa.isda.imagetools.ext.test;
/*
 * Test_ImStats.java
 *
  */

/**
 *
 * @author  Peter Bajcsy
 * @version 2.0
 */

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.ext.math.*;
import edu.illinois.ncsa.isda.imagetools.ext.statistics.*;




public class Test_ImStats extends Object {

  private ImStats _myStats = new ImStats();
  public  boolean _testPassed=true;
  // input image file name. The file's stats will be computed
  // and the stats will be printed out
  public static void main(String args[])throws Exception{

    Test_ImStats myTest = new Test_ImStats();
    //boolean ret = myTest.TestAll();
    //boolean ret = myTest.TestFloatMaskByte(new ImageObject());
    //boolean ret = myTest.TestFloatMaskInt(new ImageObject());
    boolean ret = myTest.TestAllStatsOverMaskByte();
    //boolean ret = myTest.TestAllStatsOverMaskInt(new ImageObject());

    System.out.println("Test Result = " + ret);

    /*
    System.out.println("argument length="+args.length);
    for(int i=0;i<args.length;i++){
       System.out.println("args[" + i + "]:" + args[i]);
    }

    String InFileName, OutFileName;
    InFileName = args[0];
    System.out.println(InFileName);

    OutFileName = args[1];
    System.out.println(OutFileName);
    */

  }

  // constructor
  public Test_ImStats() {
  }

  public boolean TestAll(){

    ImageObject testObject = null;
    //////////////////////////
    //prepare byte data
    try{
     testObject = testObject.createImage(10, 20, 3, "BYTE");
   }catch(Exception e){
     System.out.println("ERROR: could not create image "+e);
     return false;
   }
    int i,j,idx=0;
    for(i=0;i<testObject.getSize();i+=testObject.getNumBands()){
      for(j=0;j<testObject.getNumBands();j++){
        testObject.setByte(idx,(byte)j);
        idx++;
      }
    }

    //test
    //System.out.println("TEST: byte image \n"+ testObject.toString() );
    //System.out.println(testObject.toStringAll() );

    boolean overall = true;
    _testPassed = true;
    _testPassed = _testPassed & TestByte(testObject);
    _testPassed = _testPassed & TestCommon(testObject);
    if(testObject == null){
       System.out.println("Info: did not return created image??");
    }else{
      System.out.println("Info: Results of the test for " +
                         testObject.getTypeString() + " is " + _testPassed);
    }
    overall = overall & _testPassed;

    _testPassed = true;
    ///////////////////
    //prepare short data
/*    //_testPassed = _testPassed & TestShort(testObject);
    _testPassed = _testPassed & TestCommon(testObject);
    System.out.println("Info: Results of the test for "+testObject.getTypeString()+" is "+ _testPassed);
    overall = overall & _testPassed;

    _testPassed = true;
    //_testPassed = _testPassed & TestInt(testObject);
    _testPassed = _testPassed & TestCommon(testObject);
    System.out.println("Info: Results of the test for "+testObject.getTypeString()+" is "+ _testPassed);
    overall = overall & _testPassed;

    _testPassed = true;
    //_testPassed = _testPassed & TestLong(testObject);
    _testPassed = _testPassed & TestCommon(testObject);
    System.out.println("Info: Results of the test for "+testObject.getTypeString()+" is "+ _testPassed);
    overall = overall & _testPassed;

    _testPassed = true;
    //_testPassed = _testPassed & TestFloat(testObject);
    _testPassed = _testPassed & TestCommon(testObject);
    System.out.println("Info: Results of the test for "+testObject.getTypeString()+" is "+ _testPassed);
    overall = overall & _testPassed;

    _testPassed = true;
    //_testPassed = _testPassed & TestDouble(testObject);
    _testPassed = _testPassed & TestCommon(testObject);
    System.out.println("Info: Results of the test for "+testObject.getTypeString()+" is "+ _testPassed);
    overall = overall & _testPassed;
    //_myStats.PrintImStats();
*/
    return(overall);

  }

  public boolean TestCommon(ImageObject testObject){
    double [] im = null;
    boolean ret = true;
    ret = _myStats.MeanStdevVal(testObject);
    System.out.println("ImStats MeanStdev successful = " + ret);
    if( (im = _myStats.GetMeanVal() ) != null ){
      if(im == null || im[0] != 0.0 || im[1] !=1.0){
        System.out.println("Error: computing meanVal(0,1)="+im[0]+", "+im[1]+" Expected=(0, 1)");
        _testPassed = false;
      }
    }else{
        System.out.println("Error: did not compute meanVal");
        _testPassed = false;
    }

    if ( (im = _myStats.GetStdevVal() ) != null ) {
      if(im == null || im[0] != 0.0 || im[1] !=0.0){
        System.out.println("Error: computing stdevVal(0,1)="+im[0]+", "+im[1]+" Expected=(0, 0)");
        _testPassed = false;
      }
    }else{
        System.out.println("Error: did not compute stdevVal");
        _testPassed = false;
    }

    ret = _myStats.SkewKurtosisVal(testObject);
    System.out.println("ImStats SkewKurtosis successful = " + ret);
    if( (im = _myStats.GetSkewVal() ) != null ){
      if(im == null || im[0] != 0.0 || im[1] !=0.0){
        System.out.println("Error: computing skewVal(0,1)="+im[0]+", "+im[1]+" Expected=(0, 0)");
        _testPassed = false;
      }
    }else{
        System.out.println("Error: did not compute skewVal");
        _testPassed = false;
    }

    return (_testPassed);
  }
  // test for byte numbers
  public boolean TestByte(ImageObject testObject){
    boolean ret;
/*   try{
     testObject = testObject.createImage(10, 20, 3, "BYTE");
   }catch(Exception e){
     System.out.println("ERROR: could not create image "+e);
     return false;
   }
    int i,j,idx=0;
    for(i=0;i<testObject.getSize();i+=testObject.getNumBands()){
      for(j=0;j<testObject.getNumBands();j++){
        testObject.setByte(idx,(byte)j);
        idx++;
      }
    }

    //test
    System.out.println("TEST: byte image \n"+ testObject.toString() );
    System.out.println(testObject.toStringAll() );
  */
    //perform the test
    ret = _myStats.MinMaxVal(testObject);
    System.out.println("ImStats MinMaxVal successful = " + ret);

    ImageObject im = null;
    if( (im = _myStats.GetMinVal()) != null){
      //test
      //System.out.println("test: minVal Object fetched");
      //im.PrintImageObject();
      //_myStats.PrintImStats();

      if(im.getData() == null || im.getByte(0) != 0 || im.getByte(1) !=1 ){
        System.out.println("Error: computing minVal(0,1)="+im.getByte(0)+", "+im.getByte(1)+" Expected=(0, 1)");
        _testPassed = false;
      }
    }else{
        System.out.println("Error: did not compute minVal");
        _testPassed = false;
    }

    im = null;
    if( (im = _myStats.GetMaxVal()) != null ){
      if(im.getData() == null || im.getByte(0) != 0 || im.getByte(1) !=1){
        System.out.println("Error: computing maxVal(0,1)="+im.getByte(0)+", "+im.getByte(1)+" Expected=(0, 1)");
        _testPassed = false;
      }
    }else{
      System.out.println("Error: did not compute maxVal");
      _testPassed = false;
    }
    return(_testPassed);
  }
/*
  // test for short numbers
  public boolean TestShort(ImageObject testObject){
    boolean ret;
    testObject.numrows = 10;
    testObject.numcols = 20;
    testObject.getNumBands() = 3;
    testObject.sampType = "SHORT";
    testObject.size = testObject.numrows * testObject.numcols * testObject.getNumBands();
    testObject.imageShort = new short[(int) testObject.size];
    int i,j,idx=0;
    for(i=0;i<testObject.size;i+=testObject.getNumBands()){
      for(j=0;j<testObject.getNumBands();j++){
        testObject.imageShort[idx]= (short)j;
        idx++;
      }
    }

    //test
    //testObject.PrintImageObject();
    //perform the test
    ret = _myStats.MinMaxVal(testObject);
    System.out.println("ImStats MinMaxVal successful = " + ret);

    ImageObject im = null;
    if( (im = _myStats.GetMinVal()) != null){
      //test
      //System.out.println("test: minVal Object fetched");
      //im.PrintImageObject();
      //_myStats.PrintImStats();

      if(im.imageShort == null || im.imageShort[0] != 0 || im.imageShort[1] !=1 ){
        System.out.println("Error: computing minVal(0,1)="+im.imageShort[0]+", "+im.imageShort[1]+" Expected=(0, 1)");
        _testPassed = false;
      }
    }else{
        System.out.println("Error: did not compute minVal");
        _testPassed = false;
    }

    im = null;
    if( (im = _myStats.GetMaxVal()) != null ){
      if(im.imageShort == null || im.imageShort[0] != 0 || im.imageShort[1] !=1){
        System.out.println("Error: computing maxVal(0,1)="+im.imageShort[0]+", "+im.imageShort[1]+" Expected=(0, 1)");
        _testPassed = false;
      }
    }else{
      System.out.println("Error: did not compute maxVal");
      _testPassed = false;
    }
    return(_testPassed);
  }
  // test for integer numbers
  public boolean TestInt(ImageObject testObject){
    boolean ret;
    testObject.numrows = 10;
    testObject.numcols = 20;
    testObject.getNumBands() = 3;
    testObject.sampType = "INT";
    testObject.size = testObject.numrows * testObject.numcols * testObject.getNumBands();
    testObject.imageInt = new int[(int) testObject.size];
    int i,j,idx=0;
    for(i=0;i<testObject.size;i+=testObject.getNumBands()){
      for(j=0;j<testObject.getNumBands();j++){
        testObject.imageInt[idx]= j;
        idx++;
      }
    }

    //test
    //testObject.PrintImageObject();
    //perform the test
    ret = _myStats.MinMaxVal(testObject);
    System.out.println("ImStats MinMaxVal successful = " + ret);

    ImageObject im = null;
    if( (im = _myStats.GetMinVal()) != null){
      //test
      //System.out.println("test: minVal Object fetched");
      //im.PrintImageObject();
      //_myStats.PrintImStats();

      if(im.imageInt == null || im.imageInt[0] != 0 || im.imageInt[1] !=1 ){
        System.out.println("Error: computing minVal(0,1)="+im.imageInt[0]+", "+im.imageInt[1]+" Expected=(0, 1)");
        _testPassed = false;
      }
    }else{
        System.out.println("Error: did not compute minVal");
        _testPassed = false;
    }

    im = null;
    if( (im = _myStats.GetMaxVal()) != null ){
      if(im.imageInt == null || im.imageInt[0] != 0 || im.imageInt[1] !=1){
        System.out.println("Error: computing maxVal(0,1)="+im.imageInt[0]+", "+im.imageInt[1]+" Expected=(0, 1)");
        _testPassed = false;
      }
    }else{
      System.out.println("Error: did not compute maxVal");
      _testPassed = false;
    }
    return(_testPassed);
  }
  // test for long numbers
  public boolean TestLong(ImageObject testObject){
    boolean ret;
    testObject.numrows = 10;
    testObject.numcols = 20;
    testObject.getNumBands() = 3;
    testObject.sampType = "LONG";
    testObject.size = testObject.numrows * testObject.numcols * testObject.getNumBands();
    testObject.imageLong = new long[(int) testObject.size];
    int i,j,idx=0;
    for(i=0;i<testObject.size;i+=testObject.getNumBands()){
      for(j=0;j<testObject.getNumBands();j++){
        testObject.imageLong[idx]= j;
        idx++;
      }
    }

    //test
    //testObject.PrintImageObject();
    //perform the test
    ret = _myStats.MinMaxVal(testObject);
    System.out.println("ImStats MinMaxVal successful = " + ret);

    ImageObject im = null;
    if( (im = _myStats.GetMinVal()) != null){
      //test
      //System.out.println("test: minVal Object fetched");
      //im.PrintImageObject();
      //_myStats.PrintImStats();

      if(im.imageLong == null || im.imageLong[0] != 0 || im.imageLong[1] !=1 ){
        System.out.println("Error: computing minVal(0,1)="+im.imageLong[0]+", "+im.imageLong[1]+" Expected=(0, 1)");
        _testPassed = false;
      }
    }else{
        System.out.println("Error: did not compute minVal");
        _testPassed = false;
    }

    im = null;
    if( (im = _myStats.GetMaxVal()) != null ){
      if(im.imageLong == null || im.imageLong[0] != 0 || im.imageLong[1] !=1){
        System.out.println("Error: computing maxVal(0,1)="+im.imageLong[0]+", "+im.imageLong[1]+" Expected=(0, 1)");
        _testPassed = false;
      }
    }else{
      System.out.println("Error: did not compute maxVal");
      _testPassed = false;
    }
    return(_testPassed);
  }
  // test for float numbers
  public boolean TestFloat(ImageObject testObject){
    boolean ret;
    testObject.numrows = 10;
    testObject.numcols = 20;
    testObject.getNumBands() = 3;
    testObject.sampType = "FLOAT";
    testObject.size = testObject.numrows * testObject.numcols * testObject.getNumBands();
    testObject.imageFloat = new float[(int) testObject.size];
    int i,j,idx=0;
    for(i=0;i<testObject.size;i+=testObject.getNumBands()){
      for(j=0;j<testObject.getNumBands();j++){
        testObject.imageFloat[idx]= j;
        idx++;
      }
    }

    //test
    //testObject.PrintImageObject();
    //perform the test
    ret = _myStats.MinMaxVal(testObject);
    System.out.println("ImStats MinMaxVal successful = " + ret);

    ImageObject im = null;
    if( (im = _myStats.GetMinVal()) != null){
      //test
      //System.out.println("test: minVal Object fetched");
      //im.PrintImageObject();
      //_myStats.PrintImStats();

      if(im.imageFloat == null || im.imageFloat[0] != 0 || im.imageFloat[1] !=1 ){
        System.out.println("Error: computing minVal(0,1)="+im.imageFloat[0]+", "+im.imageFloat[1]+" Expected=(0, 1)");
        _testPassed = false;
      }
    }else{
        System.out.println("Error: did not compute minVal");
        _testPassed = false;
    }

    im = null;
    if( (im = _myStats.GetMaxVal()) != null ){
      if(im.imageFloat == null || im.imageFloat[0] != 0 || im.imageFloat[1] !=1){
        System.out.println("Error: computing maxVal(0,1)="+im.imageFloat[0]+", "+im.imageFloat[1]+" Expected=(0, 1)");
        _testPassed = false;
      }
    }else{
      System.out.println("Error: did not compute maxVal");
      _testPassed = false;
    }
    return(_testPassed);
  }

  // test for double numbers
  public boolean TestDouble(ImageObject testObject){
    boolean ret;
    testObject.numrows = 10;
    testObject.numcols = 20;
    testObject.getNumBands() = 3;
    testObject.sampType = "DOUBLE";
    testObject.size = testObject.numrows * testObject.numcols * testObject.getNumBands();
    testObject.imageDouble = new double[(int) testObject.size];
    int i,j,idx=0;
    for(i=0;i<testObject.size;i+=testObject.getNumBands()){
      for(j=0;j<testObject.getNumBands();j++){
        testObject.imageDouble[idx]= j;
        idx++;
      }
    }

    //test
    //testObject.PrintImageObject();
    //perform the test
    ret = _myStats.MinMaxVal(testObject);
    System.out.println("ImStats MinMaxVal successful = " + ret);

    ImageObject im = null;
    if( (im = _myStats.GetMinVal()) != null){
      //test
      //System.out.println("test: minVal Object fetched");
      //im.PrintImageObject();
      //_myStats.PrintImStats();

      if(im.imageDouble == null || im.imageDouble[0] != 0 || im.imageDouble[1] !=1 ){
        System.out.println("Error: computing minVal(0,1)="+im.imageDouble[0]+", "+im.imageDouble[1]+" Expected=(0, 1)");
        _testPassed = false;
      }
    }else{
        System.out.println("Error: did not compute minVal");
        _testPassed = false;
    }

    im = null;
    if( (im = _myStats.GetMaxVal()) != null ){
      if(im.imageDouble == null || im.imageDouble[0] != 0 || im.imageDouble[1] !=1){
        System.out.println("Error: computing maxVal(0,1)="+im.imageDouble[0]+", "+im.imageDouble[1]+" Expected=(0, 1)");
        _testPassed = false;
      }
    }else{
      System.out.println("Error: did not compute maxVal");
      _testPassed = false;
    }
    return(_testPassed);
  }
*/
  //////////////////////////////////
  // test for float numbers
  public boolean TestFloatMaskByte(ImageObject testObject){
    boolean ret;
  /*  testObject.numrows = 10;
    testObject.numcols = 20;
    testObject.getNumBands() = 1;
    testObject.sampType = "FLOAT";
    testObject.size = testObject.numrows * testObject.numcols * testObject.getNumBands();
    testObject.imageFloat = new float[(int) testObject.size];
   */
    try{
      testObject = testObject.createImage(10, 20, 3, "FLOAT");
    }catch(Exception e){
      System.out.println("ERROR: could not create image");
      return false;
    }
    int i,j,idx=0;
    for(i=0;i<testObject.getSize();i+=testObject.getNumBands()){
      for(j=0;j<testObject.getNumBands();j++){
        testObject.setFloat(idx, i);
        idx++;
      }
    }



    ImageObject maskObject = null;
    try{
      maskObject = maskObject.createImage(testObject.getNumRows(),
                                          testObject.getNumCols(), 1, "BYTE");
    }catch(Exception e){
      System.out.println("ERROR: could not create mask image");
      return false;
    }

    idx=0;
    for(i=0;i<maskObject.getSize();i+=maskObject.getNumBands()){
        if( i == 0 || i == testObject.getNumCols() || i == (testObject.getNumCols()<<1) )
          maskObject.setByte(idx, (byte)-1);
        else
          maskObject.setByte(idx,  (byte)0);

        idx++;
    }


    //test
    //testObject.PrintImageObject();
    //perform the test
    try{
      ret = _myStats.SetMaskObject(maskObject);
    }catch(Exception e){
      System.out.println("ERROR: could not create mask image");
      return false;
    }
    _myStats.SetMaskValByte((byte)-1);
    _myStats.SetIsMaskPresent(true);

    System.out.println("ImStats SetMask successful = " + ret);

    ret = _myStats.MinMaxVal(testObject);
    System.out.println("ImStats MinMaxVal successful = " + ret);

    ImageObject im = null;
    if( (im = _myStats.GetMinVal()) != null){
      //test
      //System.out.println("test: minVal Object fetched");
      //im.PrintImageObject();
      //_myStats.PrintImStats();

      if(im.getData() == null || im.getFloat(0) != 0  ){
        System.out.println("Error: computing minVal(0,1)="+im.getFloat(0)+" Expected=(0)");
        _testPassed = false;
      }
    }else{
        System.out.println("Error: did not compute minVal");
        _testPassed = false;
    }

    im = null;
    if( (im = _myStats.GetMaxVal()) != null ){
      if(im.getData() == null || im.getFloat(0) != (testObject.getNumCols() <<1) ){
        System.out.println("Error: computing maxVal(40)="+im.getFloat(0)+" Expected=(40)");
        _testPassed = false;
      }
    }else{
      System.out.println("Error: did not compute maxVal");
      _testPassed = false;
    }


    return(_testPassed);
  }
/*  // test for float numbers
  public boolean TestFloatMaskInt(ImageObject testObject){
    boolean ret;
    testObject.numrows = 10;
    testObject.numcols = 20;
    testObject.getNumBands() = 1;
    testObject.sampType = "FLOAT";
    testObject.size = testObject.numrows * testObject.numcols * testObject.getNumBands();
    testObject.imageFloat = new float[(int) testObject.size];
    int i,j,idx=0;
    for(i=0;i<testObject.size;i+=testObject.getNumBands()){
      for(j=0;j<testObject.getNumBands();j++){
        testObject.imageFloat[idx]= i;
        idx++;
      }
    }



    ImageObject maskObject = new ImageObject(testObject.numrows,testObject.numcols, 1,"INT");
    idx=0;
    for(i=0;i<maskObject.size;i+=maskObject.getNumBands()){
        if( i == 0 || i == testObject.numcols || i == (testObject.numcols<<1) )
          maskObject.imageInt[idx] =  300;
        else
          maskObject.imageInt[idx] =  0;

        idx++;
    }


    //test
    //testObject.PrintImageObject();
    //perform the test
    ret = _myStats.SetMaskObject(maskObject);
    _myStats.SetMaskValInt(300);
    _myStats.SetIsMaskPresent(true);

    System.out.println("ImStats SetMask successful = " + ret);

    ret = _myStats.MinMaxVal(testObject);
    System.out.println("ImStats MinMaxVal successful = " + ret);

    ImageObject im = null;
    if( (im = _myStats.GetMinVal()) != null){
      //test
      //System.out.println("test: minVal Object fetched");
      //im.PrintImageObject();
      //_myStats.PrintImStats();

      if(im.imageFloat == null || im.imageFloat[0] != 0  ){
        System.out.println("Error: computing minVal(0,1)="+im.imageFloat[0]+" Expected=(0)");
        _testPassed = false;
      }
    }else{
        System.out.println("Error: did not compute minVal");
        _testPassed = false;
    }

    im = null;
    if( (im = _myStats.GetMaxVal()) != null ){
      if(im.imageFloat == null || im.imageFloat[0] != (testObject.numcols <<1) ){
        System.out.println("Error: computing maxVal(40)="+im.imageFloat[0]+" Expected=(40)");
        _testPassed = false;
      }
    }else{
      System.out.println("Error: did not compute maxVal");
      _testPassed = false;
    }

    ret = _myStats.MeanStdevVal(testObject);
    System.out.println("ImStats MeanStdevVal successful = " + ret);
    im = null;
    if( (im = _myStats.GetMeanVal() ) != null ){
      if(im.imageDouble == null || Math.abs(im.imageDouble[0] - testObject.numcols) > 0.000001  ){
        System.out.println("Error: computing meanVal(20)="+im.imageDouble[0]+" Expected=(20)");
        System.out.println("Error: diff="+Math.abs(im.imageDouble[0] - testObject.numcols) );
        _testPassed = false;
      }
    }else{
      System.out.println("Error: did not compute meanVal");
      _testPassed = false;
    }

    im = null;
    if( (im = _myStats.GetStdevVal() ) != null ){
      if(im.imageDouble == null || Math.abs(im.imageDouble[0] - 16.329931618554518) > 0.00001  ){
        System.out.println("Error: computing stdevVal(20)="+im.imageDouble[0]+" Expected=(16.329931618554518)");
        System.out.println("Error: diff="+Math.abs(im.imageDouble[0] - 16.329931618554518) );
         _testPassed = false;
      }
    }else{
      System.out.println("Error: did not compute stdevVal");
      _testPassed = false;
    }


    return(_testPassed);
  }
*/
  //////////////////////////////////
  // test for float numbers
  public boolean TestAllStatsOverMaskByte(){
    boolean ret;
    ImageObject testObject = null;
    try{
      testObject = testObject.createImage(10, 20, 1, "BYTE");
    }catch(Exception e){
      System.out.print("ERROR: could not create image");
      return false;
    }
    int i,j,idx=0;
    for(i=0;i<testObject.getSize();i+=testObject.getNumBands()){
      for(j=0;j<testObject.getNumBands();j++){
        //testObject.imageFloat[idx]= i;
        testObject.setByte(idx,(byte)j );
        idx++;
      }
    }
    //label -1 or 255
    testObject.setByte(0,(byte)10);
    testObject.setByte(20,(byte)20);
    testObject.setByte(40,(byte)30);

    // label 120
    testObject.setByte(1,(byte)15);
    testObject.setByte(21,(byte)15);
    testObject.setByte(41,(byte)30);


    ImageObject maskObject = null;
    try{
      maskObject = maskObject.createImage(testObject.getNumRows(),
                                          testObject.getNumCols(), 1, "BYTE");
    }catch(Exception e){
      System.out.print("ERROR: could not create image");
      return false;
    }
    idx=0;
    for(i=0;i<maskObject.getSize();i+=maskObject.getNumBands()){
        if( i == 0 || i == testObject.getNumCols() || i == (testObject.getNumCols()<<1) ){
          maskObject.setByte(idx, (byte)-1);
        }else{
          if( i == 1 || i == testObject.getNumCols()+1 || i == (testObject.getNumCols()<<1)+1 ){
            maskObject.setByte(idx, (byte)120);
            System.out.println("TEST: idx="+idx+",maskObjectVal="+maskObject.getByte(idx) );
          }else{
            maskObject.setByte(idx, (byte)0);
          }
        }
        idx++;
    }

    //test
    System.out.println("TEST: testObject="+testObject.toString() );
    //System.out.println(testObject.toStringAll() );

    //test
    System.out.println("TEST: maskObject="+maskObject.toString() );
    //System.out.println(maskObject.toStringAll() );

    //perform the test
    ImageObject im  = _myStats.AllStatsOverMask(testObject,maskObject);
    if( im != null){
      System.out.println("ImStats AllStatsOverMask was successful = " );

      //test
      System.out.println("test: result");
      System.out.println(im.toString());
      System.out.println(im.toStringAll());

      //_myStats.PrintImStats();

      if((float [] )im.getData() == null){
        System.out.println("Error: expected Float");
        _testPassed = false;
      }
      // test lut
      if( im.getFloat(0) != 0.0F || im.getFloat(im.getNumBands() ) != 120.0F || im.getFloat(im.getNumBands()<<1) != 255.0F ){
        System.out.println("Error: expected LUT ID (0,120,255)="+im.getFloat(0)+" ,"+im.getFloat(im.getNumBands())+","+im.getFloat( (im.getNumBands()<<1) ) );
        _testPassed = false;
      }
      //test mean
      if( im.getFloat(2) != 0.0F || im.getFloat(2+im.getNumBands() ) != 20.0F || im.getFloat(2+(im.getNumBands()<<1)) != 20.0F ){
        System.out.println("Error: expected mean (0,20,20)="+im.getFloat(2)+" ,"+im.getFloat(2+im.getNumBands())+","+im.getFloat(2+(im.getNumBands()<<1) ) );
        _testPassed = false;
      }
      //test skew
      if( im.getFloat(4) != 0.0F || Math.abs(im.getFloat(4+im.getNumBands()) - 0.70710677)>0.00001  || im.getFloat(4+(im.getNumBands()<<1) ) != 0.0F ){
        System.out.println("Error: expected skew (0,0.70710677,0)="+im.getFloat(4)+" ,"+im.getFloat(4+im.getNumBands())+","+im.getFloat(4+(im.getNumBands()<<1) ) );
        _testPassed = false;
      }

    }else{
        System.out.println("Error: did not compute AllStatsOverMask");
        _testPassed = false;
    }


    return(_testPassed);
  }
/*  public boolean TestAllStatsOverMaskInt(ImageObject testObject){
    boolean ret;
    testObject = new ImageObject(10,20,1,"FLOAT");
    int i,j,idx=0;
    for(i=0;i<testObject.size;i+=testObject.getNumBands()){
      for(j=0;j<testObject.getNumBands();j++){
        testObject.imageFloat[idx]= j;
        //testObject.image[idx]= (byte)j;
        idx++;
      }
    }
    //label -1 or 255
    testObject.imageFloat[0] = 10;
    testObject.imageFloat[20] = 20;
    testObject.imageFloat[40] = 30;

    // label 120
    testObject.imageFloat[1] = 15;
    testObject.imageFloat[21] = 15;
    testObject.imageFloat[41] = 30;

    ImageObject maskObject = new ImageObject(testObject.numrows,testObject.numcols, 1,"INT");
    idx=0;
    for(i=0;i<maskObject.size;i+=maskObject.getNumBands()){
        if( i == 0 || i == testObject.numcols || i == (testObject.numcols<<1) ){
          maskObject.imageInt[idx] =  -1;
        }else{
          if( i == 1 || i == testObject.numcols+1 || i == (testObject.numcols<<1)+1 ){
            maskObject.imageInt[idx] =  120;
          }else{
            maskObject.imageInt[idx] =  0;
          }
        }
        idx++;
    }


    //test
    testObject.PrintImageObject();
    maskObject.PrintImageObject();
    System.out.println("Before ImStats AllStatsOverMask  " );

    //perform the test
    ImageObject im  = _myStats.AllStatsOverMask(testObject,maskObject);

    if( im != null){
      System.out.println("ImStats AllStatsOverMask was successful = " );

      //test
      System.out.println("test: result");
      im.PrintImageObject();
      im.PrintImageObjectAllValues();

      //_myStats.PrintImStats();

      if(im.imageFloat == null){
        System.out.println("Error: expected Float");
        _testPassed = false;
      }
      // test lut
      if( im.imageFloat[0] != -1.0F || im.imageFloat[im.getNumBands()] != 0.0F || im.imageFloat[im.getNumBands()<<1] != 120.0F ){
        System.out.println("Error: expected LUT ID (-1,0,120)="+im.imageFloat[0]+" ,"+im.imageFloat[im.getNumBands()]+","+im.imageFloat[im.getNumBands()<<1] );
        _testPassed = false;
      }
      //test mean
      if( im.imageFloat[2] != 20.0F || im.imageFloat[2+im.getNumBands()] != 0.0F || im.imageFloat[2+(im.getNumBands()<<1)] != 20.0F ){
        System.out.println("Error: expected mean (20,0,20)="+im.imageFloat[2]+" ,"+im.imageFloat[2+im.getNumBands()]+","+im.imageFloat[2+(im.getNumBands()<<1)] );
        _testPassed = false;
      }
      //test skew
      if( im.imageFloat[4] != 0.0F || im.imageFloat[4+im.getNumBands()] != 0.0F  || Math.abs(im.imageFloat[4+(im.getNumBands()<<1)] - 0.70710677)>0.00001  ){
        System.out.println("Error: expected skew (0,0,0.70710677)="+im.imageFloat[4]+" ,"+im.imageFloat[4+im.getNumBands()]+","+im.imageFloat[4+(im.getNumBands()<<1)] );
        _testPassed = false;
      }

    }else{
        System.out.println("Error: did not compute AllStatsOverMask");
        _testPassed = false;
    }


    return(_testPassed);
  }
*/
}
