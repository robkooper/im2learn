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
/*
 * Test_Sampling.java
 *
  */

/**
 *
 * @author  Peter Bajcsy
 * @version 1.0
 */


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.display.Im2LearnMenu;
import edu.illinois.ncsa.isda.imagetools.core.display.ImagePanel;
import edu.illinois.ncsa.isda.imagetools.core.io.*;
import edu.illinois.ncsa.isda.imagetools.ext.geo.*;




public class Test_Sampling extends Object {

  public  boolean _testPassed=true;

   private static Log logger = LogFactory.getLog(Test_Sampling.class);

  // input image file name. The file's stats will be computed
  // and the stats will be printed out
  public static void main(String args[])throws Exception{

    Test_Sampling myTest = new Test_Sampling();
    boolean ret = myTest.TestUpsampling();

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
  public Test_Sampling() {
  }

  public boolean TestUpsampling(){


    ImageObject test = null;
    try{
      test.createImage(100,100,1,"BYTE");
    }catch(Exception e){
      logger.error("ERROR: could not create an imag object");
      return false;
    }

    int i, j, index =0;
    byte color;
    byte white = -1;
    byte black = 0;
    for (i=0;i<test.getNumRows();i++){
      if(i == (i>>1)*2 )
        color = white;
      else
        color = black;
      for(j=0;j<test.getNumCols();j++){
        test.set(index,color);
        index ++;
      }
    }

    // start Im2Learn

    //ImageWriter myio = new ImageWriter();
    try{
     ImageLoader.writeImage("C:\\PeterB\temp\\Test\\testSampling.tif", test);
   }catch(Exception e){
     logger.error("ERROR: could not create an imag object");
     return false;
   }



    return true;
  }


}
