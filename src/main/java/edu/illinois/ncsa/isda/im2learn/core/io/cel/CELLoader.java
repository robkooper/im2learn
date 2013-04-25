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
package edu.illinois.ncsa.isda.im2learn.core.io.cel;


import java.io.*;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageException;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.datatype.SubArea;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageLoader;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageReader;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageWriter;

/**
 * This class reads and writes CEL files obtained by Affymetrix microarray technology.
 *
// typical header
  [CEL]
  Version=3

  [HEADER]
  Cols=712
  Rows=712
  TotalX=712
  TotalY=712
  OffsetX=0
  OffsetY=0
  GridCornerUL=225 230
  GridCornerUR=4496 224
  GridCornerLR=4489 4495
  GridCornerLL=218 4501
  Axis-invertX=0
  AxisInvertY=0
  swapXY=0
  DatHeader=[0..46101]  #012'E 430A:CLS=4733 RWS=4733 XIN=3  YIN=3  VE=17        2.0 12/17/03 10:51:12       MOE430A.1sq                  6
  Algorithm=Percentile
  AlgorithmParameters=Percentile:75;CellMargin:2;OutlierHigh:1.500;OutlierLow:1.004

  [INTENSITY]
  NumberCells=506944
  CellHeader=X	Y	MEAN	STDV	NPIXELS
  0	  0	178.0	29.8	 16
  1	  0	9461.0	1805.2	 16

  *

 * @author Peter Bajcsy
 * @version 2.0
 */
public class CELLoader implements ImageReader {
    private static int sampleread = 10000;
    private int _numHeaderLines = 0;

    /**
     * Returns true if the file contains "CEL" as the first 3 bytes of the
     * file.
     *
     * @param filename ignored.
     * @param hdr      the first 100 bytes of the file.
     * @return true if the file can be read by this class.
     */
    public boolean canRead(String filename, byte[] hdr) {
        // file should always start with CEL
        if (hdr[0] == '[' && hdr[1] == 'C' && hdr[2] == 'E'&& hdr[3] == 'L' && hdr[4] == ']') {
            return true;
        }

        // no matching on ext, if it does not start with CEL we can't read it.
        return false;
    }

    /**
     * This function will read the file and return an imageobject that contains
     * the file.
     *
     * @param filename of the file to be read
     * @param subarea  of the file to be read, null if full image is to be
     *                 read.
     * @param sampling is the subsampling that needs to be done on the image as
     *                 it is loaded.
     * @return the file as an imageobject.
     * @throws IOException if the file could not be read.
     */
    public ImageObject readImage(String filename, SubArea subarea, int sampling) throws IOException {
        if(filename == null){
          throw(new IOException("filename cannot be null."));
        }
        //System.out.println("Input CEL file name = " + filename);

         File headerfile = null;

         if (filename.toLowerCase().endsWith(".cel")) {
             headerfile = new File(filename);
             if (!headerfile.exists()) {
                 headerfile = null;
             }
         }
         if (headerfile == null) {
             throw(new IOException("Could not find CEL file."));
         }

         _numHeaderLines = 0;
         ImageObject imgobj = readImageHeader(filename);
         if(imgobj == null){
           return null;
         }
         if(!loadImageData(filename, imgobj) ){
           throw(new IOException("Could not load data from CEL file."));
         }

         // TODO improve scale/crop
         if (subarea != null) {
             try {
                 imgobj = imgobj.crop(subarea);
             } catch (ImageException e) {
                 throw(new IOException("Could not crop image."));
             }
         }
         if (sampling != 1.0) {
             try {
                 imgobj.scale(sampling);
             } catch (ImageException e) {
                 throw(new IOException("Could not scale image."));
             }
         }

         imgobj.computeMinMax();
         return imgobj;


    }

      /**
     * Return a list of extentions this class can read.
     *
     * @return a list of extentions that are understood by this class.
     */
    public String[] readExt() {
        return new String[]{"CEL"};
    }

    /**
     * Return the description of the reader (or writer).
     *
     * @return decription of the reader (or writer)
     */
    public String getDescription() {
        return "CEL files";
    }
   //
   // TODO support generic header reading
   public ImageObject readImageHeader(String filename) throws IOException {

     File headerfile = new File(filename);
     if (!headerfile.exists()) {
            headerfile = null;
     }
     if (headerfile == null) {
        throw(new IOException("Could not find CEL file."));
    }

    // load the file
     BufferedReader r = new BufferedReader(new FileReader(headerfile));
     int numrows =0;
     int numcols = 0;
     int numbands = 0;
     int type = ImageObject.TYPE_FLOAT;
     int numcells = 0;
     String[] split = null;
     // load the file
     String line = r.readLine();
     int lineno = 1;
     while (line != null) {
         line.trim();
         split = line.split("[ ]*=[ ]*", 2);

         // First line should always be [CEL]
         if (lineno == 1) {
             if (split[0].trim().compareToIgnoreCase("[CEL]") != 0) {
                  throw(new IOException("First line in headerfile should be [CEL]."));
                 //System.err.println("First line in headerfile should be [CEL].");
                 //return null;
             }
             // read next line (Version)
             lineno++;
             line = r.readLine();
             line.trim();
             split = line.split("[ ]*=[ ]*", 2);
         }

         if (split[0].compareToIgnoreCase("[HEADER]") == 0) {
           //read next two line
           lineno++;
           line = r.readLine();
           line.trim();
           split = line.split("[ ]*=[ ]*", 2);
           if (split[0].trim().compareToIgnoreCase("Cols") == 0) {
             numcols = Integer.parseInt(split[1]);
           }

           lineno++;
           line = r.readLine();
           line.trim();
           split = line.split("[ ]*=[ ]*", 2);
           if (split[0].trim().compareToIgnoreCase("Rows") == 0) {
             numrows = Integer.parseInt(split[1]);
           }
         }

         if (split[0].compareToIgnoreCase("[INTENSITY]") == 0) {
           //read next lines
           lineno++;
           line = r.readLine();
           line.trim();
           split = line.split("[ ]*=[ ]*", 2);
           if (split[0].trim().compareToIgnoreCase("NumberCells") == 0) {
            numcells = Integer.parseInt(split[1]);
            //check
            if(numcells != numrows * numcols){
              throw(new IOException("ERROR: number of cells="+numcells+" does not match numrows="+numrows+" * numcols="+numcols));

             //System.err.println("ERROR: number of cells="+numcells+" does not match numrows="+numrows+" * numcols="+numcols);
             //return null;
            }

           }

           lineno++;
           line = r.readLine();
           line.trim();
           split = line.split("[ ]*=[ ]*", 2);
           if (split[0].trim().compareToIgnoreCase("CellHeader") == 0) {

            numbands = 0;
             line = split[1];
             split = line.split("[\\s]*");
             //System.out.println("TEST: length = "+ split.length);

             for(int i=0;i<split.length;i++){
               //System.out.println("split[" + i + "]=" + split[i]);
               if(split[i].compareTo("") == 0)
                 numbands ++;
             }
             //System.out.println();

             // subtract X and Y add mask and outlier bands
             //numbands  = numbands - 2 +1 +1;
             System.out.println("TEST: numbands = "+ numbands);

           }
         }

         if (split[0].compareToIgnoreCase("[MODIFIED]") == 0) {
           //read next lines
           lineno++;
           line = r.readLine();
           line.trim();
           split = line.split("[ ]*=[ ]*", 2);
           if (split[0].trim().compareToIgnoreCase("NumberCells") == 0) {
            numcells = Integer.parseInt(split[1]);
            System.out.println("TEST: number of cells modified =" +numcells);
           }

           lineno++;
           line = r.readLine();
           line.trim();
           split = line.split("[ ]*=[ ]*", 2);
           if (split[0].trim().compareToIgnoreCase("CellHeader") == 0) {
              System.out.println("TEST: number of attributes  modified =" + (split.length - 1) );
           }
         }

         if(numbands > 0 ){
           // the next line is data

           line = null;
         }else{
           lineno++;
           line = r.readLine();
         }
      }
      r.close();
      if (numrows <= 0 || numcols <= 0 || numbands <= 0){
         System.err.println("ERROR: could not find information about Cols, Rows or Bands; ");
         System.err.println("ERROR: Cols="+numcols+", Rows="+numrows+", Bands="+numbands);
         return null;
      }

      _numHeaderLines =  lineno;
      ImageObject imgobj = null;
      try{
        imgobj = ImageObject.createImage(numrows, numcols, numbands, type);
      }catch(Exception e){
        System.err.println("ERROR: image object could not be created");
        return null;
      }
      // set all pixels to black
      for(int i=0;i<imgobj.getSize();i++)
        imgobj.set(i,0.0);

      return imgobj;
   }

  ////////////////////////////////
  private boolean loadImageData(String filename, ImageObject imgobj) throws IOException {

    // sanity check
     if (imgobj == null || imgobj.getNumRows() <= 0 || imgobj.getNumCols() <= 0 || imgobj.getNumBands() <= 0){
        return false;
     }

     File headerfile = new File(filename);
     if (!headerfile.exists()) {
            headerfile = null;
     }
     if (headerfile == null) {
        throw(new IOException("Could not find CEL file."));
    }

    // load the file
     BufferedReader r = new BufferedReader(new FileReader(headerfile));
     int row=0, col=0, index, i,j;
     float temp;
     String[] split = null;
     // load the file
     int lineno = 1;
     String line = r.readLine();
     boolean readIntensityData = false;
     boolean readMaskData = false;
     boolean readOutlierData = false;
     int numMaskCells = 0;
     int numOutlierCells = 0;

     String tempString = new String("");
     int entry=0,band=0;
     //test
     //int count =0;
     //while (line != null && count < 10) {
     while (line != null) {
       if (line.compareToIgnoreCase("[INTENSITY]") == 0) {
         //skip next two lines and switch to readIntensityData=true mode
         lineno++;
         line = r.readLine();
         lineno++;
         line = r.readLine();
         lineno++;
         line = r.readLine();
         readIntensityData = true;
       }

       if (line.compareToIgnoreCase("[MASKS]") == 0) {
         // mask data
         //read next lines
         lineno++;
         line = r.readLine();
         line.trim();
         split = line.split("[ ]*=[ ]*", 2);
         if (split[0].trim().compareToIgnoreCase("NumberCells") == 0) {
          numMaskCells = Integer.parseInt(split[1]);
         }

         lineno++;
         line = r.readLine();
         line.trim();
         split = line.split("[ ]*=[ ]*", 2);
         if (split[0].trim().compareToIgnoreCase("CellHeader") == 0) {
           j = 0;
           line = split[1];
           split = line.split("[\\s]*");
           //System.out.println("TEST: length = "+ split.length);

           for(i=0;i<split.length;i++){
             //System.out.println("split[" + i + "]=" + split[i]);
             if(split[i].compareTo("") == 0)
               j ++;
           }
           //test
           System.out.println("INFO: number of mask cells="+numMaskCells+" numBands="+j);
           // there should be only  X and Y
           if(j!=2){
             throw(new IOException("ERROR: number of mask cells="+numMaskCells+" numBands"+j));
           }

         }
         lineno++;
         line = r.readLine();
         readIntensityData = false;
         readOutlierData = false;
         readMaskData = true;
       }


       if (line.compareToIgnoreCase("[OUTLIERS]") == 0) {
         // outlier data
         //read next lines
         lineno++;
         line = r.readLine();
         line.trim();
         split = line.split("[ ]*=[ ]*", 2);
         if (split[0].trim().compareToIgnoreCase("NumberCells") == 0) {
          numOutlierCells = Integer.parseInt(split[1]);
         }

         lineno++;
         line = r.readLine();
         line.trim();
         split = line.split("[ ]*=[ ]*", 2);
         if (split[0].trim().compareToIgnoreCase("CellHeader") == 0) {
           j = 0;
           line = split[1];
           split = line.split("[\\s]*");
           //System.out.println("TEST: length = "+ split.length);

           for(i=0;i<split.length;i++){
             //System.out.println("split[" + i + "]=" + split[i]);
             if(split[i].compareTo("") == 0)
               j ++;
           }
           //test
           System.out.println("INFO: number of outlier cells="+numOutlierCells+" numBands="+j);
           // there should be only  X and Y
           if(j!=2){
             throw(new IOException("ERROR: number of outlier cells="+numOutlierCells+" numBands"+j));
           }

         }
         lineno++;
         line = r.readLine();
         readIntensityData = false;
         readMaskData = false;
         readOutlierData = true;
       }

       if (line.compareToIgnoreCase("[MODIFIED]") == 0) {
         // the rest of the file is not data to read
         readIntensityData = false;
       }

         if(readIntensityData){
           //test
             //count++;
             for(j=0;j<imgobj.getNumCols()*imgobj.getNumCols();j++){
                 line.trim();
                 split = line.split("[\\s]*");
                 //System.out.println("TEST: length = "+ split.length+" lastSplit="+split[split.length-1]);
                 entry=band=0;

                 for(i=0;i<split.length;i++){
                   //System.out.println("split[" + i + "]=" + split[i]);
                   if(split[i].compareTo("")==0 || i==split.length-1){
                     if(tempString.compareTo("") != 0){
                       if(i==split.length-1)
                         tempString += split[split.length-1];

                       //System.err.println("TEST: tempString = "+tempString);


                       switch (entry){
                         case 0:
                           col = Integer.parseInt(tempString);
                           if(col <0 || col >= imgobj.getNumCols())
                             System.err.print("ERROR: col is out bounds col="+col);
                           break;
                         case 1:
                           row = Integer.parseInt(tempString);
                           if(row <0 || row >= imgobj.getNumRows())
                             System.err.print("ERROR: row is out bounds row="+row);
                           break;
                         default:
                           temp = Float.parseFloat(tempString);
                           //test
                           if(temp < 0 || temp > 0xffff  ){
                             System.out.println("TEST: val is out bounds: row = "+row+", col="+col+", band="+band+", val="+temp);
                           }
                           if(band >=0 && band<imgobj.getNumBands()){
                              //System.out.println("TEST: imgobj row = "+row+", col="+col+", band="+band+", val="+temp);

                              imgobj.set(row,col,band,temp);
                           }else{
                             //System.err.println("ERROR: imgobj row = "+row+", col="+col+" are out of bounds");
                             //System.err.println("ERROR: imgobj numbands = "+band);
                           }
                           band++;
                           break;
                       }// end of switch
                       entry++;
                     }// end of parsing
                     tempString = "";
                   }else{
                     tempString += split[i];
                   }
                 }
                 lineno++;
                 line = r.readLine();
             }// end of j loop
             readIntensityData = false;

         }// if (readIntensityData)
         if(readMaskData){
           //test
             //count++;
             for(j=0;j<numMaskCells;j++){
                 line.trim();
                 split = line.split("[\\s]*");
                 //System.out.println("TEST: length = "+ split.length+" lastSplit="+split[split.length-1]);
                 entry=band=0;

                 for(i=0;i<split.length;i++){
                   //System.out.println("split[" + i + "]=" + split[i]);
                   if(split[i].compareTo("")==0 || i==split.length-1){
                     if(tempString.compareTo("") != 0){
                       if(i==split.length-1)
                         tempString += split[split.length-1];

                       //System.err.println("TEST: tempString = "+tempString);
                       switch (entry){
                         case 0:
                           col = Integer.parseInt(tempString);
                           if(col <0 || col >= imgobj.getNumCols())
                             System.err.print("ERROR: col is out bounds col="+col);
                           break;
                         case 1:
                           row = Integer.parseInt(tempString);
                           if(row <0 || row >= imgobj.getNumRows())
                             System.err.print("ERROR: row is out bounds row="+row);


                           temp = 255;
                           imgobj.set(row,col,imgobj.getNumBands()-2,temp);
                           //test
                           //System.out.println("TEST: mask row = "+row+", col="+col+", val="+temp);
                           break;
                         default:
                           break;
                       }// end of switch
                       entry++;
                     }// end of parsing
                     tempString = "";
                   }else{
                     tempString += split[i];
                   }
                 }
                 lineno++;
                 line = r.readLine();
             }// end of j loop
             readMaskData = false;

         }// if (readMaskData)
         if(readOutlierData){
           //test
             //count++;
             for(j=0;j<numOutlierCells;j++){
                 line.trim();
                 split = line.split("[\\s]*");
                 //System.out.println("TEST: length = "+ split.length+" lastSplit="+split[split.length-1]);
                 entry=band=0;

                 for(i=0;i<split.length;i++){
                   //System.out.println("split[" + i + "]=" + split[i]);
                   if(split[i].compareTo("")==0 || i==split.length-1){
                     if(tempString.compareTo("") != 0){
                       if(i==split.length-1)
                         tempString += split[split.length-1];

                       //System.err.println("TEST: tempString = "+tempString);
                       switch (entry){
                         case 0:
                           col = Integer.parseInt(tempString);
                           if(col <0 || col >= imgobj.getNumCols())
                             System.err.print("ERROR: col is out bounds col="+col);
                           break;
                         case 1:
                           row = Integer.parseInt(tempString);
                           if(row <0 || row >= imgobj.getNumRows())
                             System.err.print("ERROR: row is out bounds row="+row);


                           temp = 255;
                           imgobj.set(row,col,imgobj.getNumBands()-1,temp);
                           //test
                           //System.out.println("TEST: mask row = "+row+", col="+col+", val="+temp);
                           break;
                         default:
                           break;
                       }// end of switch
                       entry++;
                     }// end of parsing
                     tempString = "";
                   }else{
                     tempString += split[i];
                   }
                 }
                 lineno++;
                 line = r.readLine();
             }// end of j loop
             readOutlierData = false;

         }// if (readOutlierData)

         lineno++;
         line = r.readLine();
      }
      r.close();

//test
      //System.out.print("TEST imgobj="+imgobj.toStringRow(0));
      return true;
   }
  
	public int getImageCount(String filename) {
		return 1;
	}

	public ImageObject readImage(String filename, int index, SubArea subarea, int sampling) throws IOException, ImageException {
		return readImage(filename, subarea, sampling);
	}

	public ImageObject readImageHeader(String filename, int index) throws IOException, ImageException {
		return readImageHeader(filename);
	}
}
