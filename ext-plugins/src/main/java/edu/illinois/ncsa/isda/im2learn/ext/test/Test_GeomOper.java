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
package edu.illinois.ncsa.isda.im2learn.ext.test;


/*
 * Test_GeomOper.java
 *
  */

/**
 *
 * @author  Peter Bajcsy
 * @version 2.0
 */



import java.awt.*;
import java.awt.image.BufferedImage;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.ext.math.*;
import edu.illinois.ncsa.isda.im2learn.ext.statistics.*;


public class Test_GeomOper extends Object {


  public static void main(String args[])throws Exception{

    boolean testPassed=true;
    Test_GeomOper myTest = new Test_GeomOper();
    testPassed = myTest.DistributionTest();
    //testPassed = myTest.EuclideanDistanceTest();

    System.out.println("Test Result = " + testPassed);


  }
    /** Creates new MyClass */
  public Test_GeomOper() {
  }

   public boolean EuclideanDistanceTest(){
     GeomOper myGeom = new GeomOper();
     ImageObject img = null;
     try{
       img = img.createImage(1, 2, 3, "BYTE");
     }catch(Exception e){
       System.err.println("ERROR: create image failed");
       return false;
     }
     boolean ret = true;
     ////////////////////////////
     //test 1
     //pts 1
     int i;
     //img.image[0] = img.image[1] = img.image[2] = 0;
     for(i=0;i<3;i++)
       img.set(i,0);
     //pts 2
     //img.image[3] = img.image[4] = img.image[5] = 1;
     for(i=3;i<6;i++)
       img.set(i,1);
     System.out.println("TEST1 input points = ");
     System.out.println(img.toString());
     System.out.println(img.toStringRow(0));
     System.out.println(img.toStringCol(1));
     System.out.println(img.toStringAll() );

     double dist = -1.0;
     dist = myGeom.euclidDist(img.getNumBands(),(byte [])img.getData(),0,(byte [])img.getData(),3);
     double expectedDist = Math.sqrt(3);
     System.out.println("TEST1 output dist = "+dist+", expectedDist="+expectedDist);
     if(Math.abs(dist - expectedDist) > 0.00001){
       System.out.println("Failed output dist = "+dist+", expectedDist="+expectedDist);
       ret = false;
     }
     ////////////////////////////
     //test 2
     //pts 2
     //img.image[3] = img.image[4] = img.image[5] = -1;
     for(i=3;i<6;i++)
       img.set(i,-1);

     System.out.println("TEST2 input points = ");
     //img.PrintImageObjectAllValues();
     System.out.println(img.toString());


     dist = -1.0;
     //dist = myGeom.EuclidDist(img.sampPerPixel,img.image,0,img.image,3);
     dist = myGeom.euclidDist(img.getNumBands(),(byte [])img.getData(),0,(byte [])img.getData(),3);

     expectedDist = Math.sqrt(3*255*255);
     System.out.println("TEST2 output dist = "+dist+", expectedDist="+expectedDist);
     if(Math.abs(dist - expectedDist) > 0.00001){
       System.out.println("Failed output dist = "+dist+", expectedDist="+expectedDist);
       ret = false;
     }

     return ret;
   }

   public boolean DistributionTest(){
     GeomOper myGeom = new GeomOper();
     ImStats myStats = new ImStats();
     boolean testPassed = true;
     //test selecting probability distr.
     String str;
     String trueString = "Impossible Distribution";
     System.out.println("Test:="+trueString);
     str = myStats.SelectDistribution(Math.sqrt(3.0), (2.0 - 3.0));
     if( !str.equalsIgnoreCase(trueString) ){
       testPassed = false;
       System.out.println("Error: did not compute correct distr.");
       System.out.println("Expected="+trueString + " Returned="+str);
     }

     trueString = "Uniform Distribution";
     System.out.println("Test:="+trueString);
     str = myStats.SelectDistribution(0.0, (1.75 - 3.0));
     if( !str.equalsIgnoreCase(trueString) ){
       testPassed = false;
       System.out.println("Error: did not compute correct distr.");
       System.out.println("Expected= "+trueString + " Returned= "+str);
     }

     trueString = "Normal Distribution";
     str = myStats.SelectDistribution(0.0, 0.0);
     if( !str.equalsIgnoreCase(trueString) ){
       testPassed = false;
       System.out.println("Error: did not compute correct distr.");
       System.out.println("Expected="+trueString + " Returned="+str);
     }

     trueString = "Exponential Distribution";
     str = myStats.SelectDistribution(2.0, (9.0 - 3.0));
     if( !str.equalsIgnoreCase(trueString) ){
       testPassed = false;
       System.out.println("Error: did not compute correct distr.");
       System.out.println("Expected="+trueString + " Returned="+str);
     }

     trueString = "Beta (U-Shaped) Distribution";
     str = myStats.SelectDistribution(2.0,(5.2 - 3.0));
     if( !str.equalsIgnoreCase(trueString) ){
       testPassed = false;
       System.out.println("Error: did not compute correct distr.");
       System.out.println("Expected="+trueString + " Returned="+str);
     }

     trueString = "Beta (J-Shaped) Distribution";
     str = myStats.SelectDistribution(2.0,(6.2 - 3.0));
     if( !str.equalsIgnoreCase(trueString) ){
       testPassed = false;
       System.out.println("Error: did not compute correct distr.");
       System.out.println("Expected="+trueString + " Returned="+str);
     }
     trueString = "Beta Distribution";
     str = myStats.SelectDistribution(0.7,0.0);
     if( !str.equalsIgnoreCase(trueString) ){
       testPassed = false;
       System.out.println("Error: did not compute correct distr.");
       System.out.println("Expected="+trueString + " Returned="+str);
     }

     trueString = "Lognormal (Gamma, Weibull) Distribution";
     System.out.println("Test:="+trueString);
     str = myStats.SelectDistribution(2.0,(9.2 - 3.0));
     if( !str.equalsIgnoreCase(trueString) ){
       testPassed = false;
       System.out.println("Error: did not compute correct distr.");
       System.out.println("Expected="+trueString + " Returned="+str);
     }

     trueString = "Student's t-Distribution";
     System.out.println("Test:="+trueString);
     str = myStats.SelectDistribution(1.0,(9.2 - 3.0));
     if( !str.equalsIgnoreCase(trueString) ){
       testPassed = false;
       System.out.println("Error: did not compute correct distr.");
       System.out.println("Expected="+trueString + " Returned="+str);
     }

     return testPassed;
   }
}
