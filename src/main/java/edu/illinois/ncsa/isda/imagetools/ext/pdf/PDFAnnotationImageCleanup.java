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
package edu.illinois.ncsa.isda.imagetools.ext.pdf;

//import ncsa.im2learn.core.datatype.ImageObject;
import java.util.Iterator;
import java.util.Vector;

import edu.illinois.ncsa.isda.imagetools.core.io.pdf.PDFAnnotation;


/**
 * Created by IntelliJ IDEA. User: bajcsy Date: May 2, 2005 Time: 9:57:38 AM To
 * change this template use File | Settings | File Templates.
 */
public class PDFAnnotationImageCleanup {
    private Vector<PDFAnnotation> annotations;
    private double dx;
    private double dy;

    public PDFAnnotationImageCleanup() {
        annotations = new Vector<PDFAnnotation>();

        dx = 0;
        dy = 0;
    }

    public PDFAnnotationImageCleanup(Vector<PDFAnnotation> annotations) {
        this.annotations = annotations; 

        dx = 0;
        dy = 0;
    }
    public void setTolerance(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public double getToleranceDX() {
        return dx;
    }

    public double getToleranceDY() {
        return dy;
    }

    public void reset(Vector<PDFAnnotation> annotations) {
        this.annotations = annotations;
    }

    // ------------------------------------------------------------------------
    // Clean PDF Images
    // ------------------------------------------------------------------------
    // remove all contained images
    public void removeContainedImages() {
    	
    	//removeInvalidImagesMinDimension(37,37 );
        // find all contained images
        Vector newtxt = new Vector();
        for (Iterator iterold = annotations.iterator(); iterold.hasNext();) {
            PDFAnnotation objold = (PDFAnnotation) iterold.next();
            if (objold.isImage() && !objold.isInvalid() && !objold.isDuplicate()) {
                for (Iterator iternew = newtxt.iterator(); iternew.hasNext() && !objold.isDuplicate();) {
                    PDFAnnotation objnew = (PDFAnnotation) iternew.next();
                    if (objnew.isImage() && !objnew.isInvalid() ){// && objnew.getObject().equals(objold.getObject())) {
                        if (checkContainedLocation(objold, objnew)) {
                           ///test
                            //System.out.println("TEST: objold "+objold);
                            //System.out.println("TEST: objnew "+objnew);
                            objold.setDuplicate(true);
                            objold.setClassification(PDFAnnotation.IMG_CONTAINED);
                            objold.addProximityIndex(objnew);
                        }
                       /* else{
                          ///test
                           System.out.println("TEST: failed objold "+objold);
                           System.out.println("TEST: failed objnew "+objnew);

                        }
                        */
                    }
                }
            }
            newtxt.add(objold);
        }
    }
    ///////////////////////////////////
    // check if the first image contains the second image or the other way
      private boolean checkContainedLocation(PDFAnnotation anno1, PDFAnnotation anno2) {
          return ( checkFirstContainsSecond(anno1, anno2) || checkSecondContainsFirst(anno1,anno2) );
      }
      private boolean checkFirstContainsSecond(PDFAnnotation anno1, PDFAnnotation anno2){
          return (anno1.getX() <= anno2.getX() && anno1.getY()<= anno2.getY() &&
                  anno1.getY() + anno1.getHeight() >= anno2.getY() + anno2.getHeight()
                  && anno1.getX()+anno1.getWidth() >= anno2.getX()+anno2.getWidth());
      }
      private boolean checkSecondContainsFirst(PDFAnnotation anno1, PDFAnnotation anno2){
          return (anno1.getX() >= anno2.getX() && anno1.getY() >= anno2.getY() &&
                  anno1.getY() + anno1.getHeight() <= anno2.getY() + anno2.getHeight()
                  && anno1.getX()+anno1.getWidth() <= anno2.getX()+anno2.getWidth());

      }
    ////////////////////////////////
      /**
       * remove all invalid  annotations based on minimum dimension criteria
       */
      public void removeInvalidImagesMinDimension(double minWidth, double minHeight ) {
          // find all invalid images
          for (Iterator iterold = annotations.iterator(); iterold.hasNext();) {
              PDFAnnotation objold = (PDFAnnotation) iterold.next();
              if( objold.isImage()){
            	  if ( objold.getWidth() < minWidth || objold.getHeight() < minHeight ){
            		  ///test
            		  System.out.println("TEST: invalid MinDim obj "+objold);
            		  objold.setInvalid(true);
            		  objold.setClassification(PDFAnnotation.DIM_INVALID);
            	  }
              }
          }
      }
      /**
       * remove all invalid  annotations based on minimum area criterion
       */
      public void removeInvalidImagesMinArea(double minArea ) {
          // find all invalid images
          for (Iterator iterold = annotations.iterator(); iterold.hasNext();) {
              PDFAnnotation objold = (PDFAnnotation) iterold.next();
              if( objold.isImage()){
            	  if ( objold.getWidth() * objold.getHeight() < minArea ){
            		  ///test
            		  System.out.println("TEST: invalid MinArea obj "+objold);
            		  objold.setInvalid(true);
            		  objold.setClassification(PDFAnnotation.AREA_INVALID);
            	  }
              }
          }
      }
  //////////////////////////////////////
  // ------------------------------------------------------------------------
      /**
      *  remove all chopped images
      *  chopped images are identified if their pass  the proximity check of the borders defined by dx and dy
      *  
       */
  public void mergeChoppedImages() {
      // find all images that are touching each other along their borderlines
      Vector newimg = new Vector();
      for (Iterator iterold = annotations.iterator(); iterold.hasNext();) {
          PDFAnnotation objold = (PDFAnnotation) iterold.next();
          if (objold.isImage() && !objold.isDuplicate() && !objold.isInvalid()) {
              boolean ignore = false;
              for (Iterator iternew = newimg.iterator(); iternew.hasNext() && !ignore;) {
                  PDFAnnotation objnew = (PDFAnnotation) iternew.next();
                  if (objnew.isImage() && !objnew.isInvalid() && (checkProximityImage(objold, objnew) || checkProximityImage(objnew, objold)) ) {

                    //test
                    //System.out.println("TEST chopped: before objold "+objold);
                    //System.out.println("TEST chopped: before objnew "+objnew);

                      //objold.setClassification(PDFAnnotation.IMG_CHOPPED);
                      objnew.addChoppedImages(objold,dx,dy);
                      objnew.setClassification(PDFAnnotation.IMG_CHOPPED);

                      //System.out.println("TEST chopped: after objold "+objold);
                      //System.out.println("TEST chopped: after objnew "+objnew);
                      //System.out.println("TEST chopped: ================");

                      ignore = true;
                  }
              }
              if (!ignore) {
                  newimg.add(objold);
              } else {
                  iterold.remove();
              }
          } else {
              newimg.add(objold);
          }
      }

    }

  // if two images share one border line then it returns true
  // else false
  private boolean checkProximityImage(PDFAnnotation anno1, PDFAnnotation anno2) {

       // check if the objects are images
       if (!anno1.isImage() || !anno2.isImage()) {
        return false;
       }
       ////////////////////left column
        //below of image1 aligned with left column
        if ( Math.abs(anno1.getX() - anno2.getX() )<=dx
             && Math.abs(anno1.getY() + anno1.getHeight() - anno2.getY() )<=dy ){
          return true;
        }
        //above of image1 aligned with left column
        if ( Math.abs(anno1.getX() - anno2.getX()) <= dx
             && Math.abs(anno1.getY()- (anno2.getY() + anno2.getHeight() ) ) <= dy  ) {
          return true;
        }
        ////////////////////right column
        //below of image1 aligned with right column
        if (  Math.abs(anno1.getX()+anno1.getWidth() - (anno2.getX()+anno2.getWidth()) )<=dx
              && Math.abs(anno1.getY() - anno2.getY() )<=dy ){
          return true;
        }
        //above of image1 aligned with right column
        if (  Math.abs(anno1.getX()+anno1.getWidth() - (anno2.getX()+anno2.getWidth()) )<=dx
              && Math.abs(anno1.getY() - (anno2.getY() + anno2.getHeight())  )<=dy ){
          return true;
        }
        ////////////////////left side of image1
        //upper left of image1
        if (  Math.abs(anno1.getX() - (anno2.getX()+anno2.getWidth()) )<=dx
              && Math.abs(anno1.getY() - anno2.getY() )<=dy ){
          return true;
        }
        //lower left of image1
        if (  Math.abs(anno1.getX() - (anno2.getX()+anno2.getWidth()) )<=dx
              && Math.abs(anno1.getY() + anno1.getHeight() - (anno2.getY() + anno2.getHeight()) )<=dy   ){
          return true;
        }
        ////////////////////right side of image1
        //upper right of image1
        if (  Math.abs(anno1.getX()+ anno1.getWidth() - anno2.getX())<=dx
              && Math.abs(anno1.getY() - anno2.getY() )<=dy ){
          return true;
        }
        //lower right of image1
        if ( Math.abs(anno1.getX() +anno1.getWidth() - anno2.getX())<=dx
             && Math.abs(anno1.getY() + anno1.getHeight() - (anno2.getY() + anno2.getHeight())  )<=dy ){
          return true;
        }

/*
        // should have at least one coordinate of the bounding box identical
        //above or below of image1 aligned with left column
        if ( Math.abs(anno1.getX() - anno2.getX() )<dx &&
             ( Math.abs(anno1.getY() + anno1.getHeight() - anno2.getY() ) < dy
               || Math.abs(anno1.getY() - (anno2.getY() + anno2.getHeight()) )  < dy) )
             return true;

       //left or right of image1 aligned with upper row
        if ( Math.abs(anno1.getY() - anno2.getY() )<dy &&
                ( Math.abs(anno1.getX() + anno1.getWidth() - anno2.getX() ) <dx ||
                  Math.abs(anno1.getX() - (anno2.getX() + anno2.getWidth()) ) <dx ) )
                return true;


         //above or below of image1 aligned with right column
         if ( Math.abs(anno1.getX() + anno1.getWidth() - anno2.getX() )<dx &&
              ( Math.abs(anno1.getY() + anno1.getHeight() - anno2.getY() )<dy ||
                Math.abs(anno1.getY() - (anno2.getY() + anno2.getHeight()) ) <dy) )
              return true;

        //left or right of image1 aligned with lower row
         if ( Math.abs(anno1.getY() + anno1.getHeight() - (anno2.getY() + anno2.getHeight()) )<dy &&
             ( Math.abs(anno1.getX() + anno1.getWidth() - anno2.getX() )<dx ||
               Math.abs(anno1.getX() - (anno2.getX() + anno2.getWidth()) )<dx ) )
             return true;
*/
        return false;
    }
    //////////////////////////////////////
    // ------------------------------------------------------------------------
  /**
   * remove all overlapped images
   * overlapped images are identified if their bounding boxes overlap in row and column dimensions
   * by more than dx % along row dimension and dy % along column dimension
   *          if (  percentRow*100 > dy && percentColumn* 100 > dx) then overlap
   *
   */
    public void mergeOverlappedImages() {

        Vector newtxt = new Vector();
        for (Iterator iterold = annotations.iterator(); iterold.hasNext();) {
            PDFAnnotation objold = (PDFAnnotation) iterold.next();
            if (objold.isImage() && !objold.isDuplicate() && !objold.isInvalid() ) {
                boolean ignore = false;
                for (Iterator iternew = newtxt.iterator(); iternew.hasNext() && !ignore;) {
                    PDFAnnotation objnew = (PDFAnnotation) iternew.next();
                    if (objnew.isImage() && !objnew.isInvalid() && checkOverlap(objold, objnew) ) {
                      //test
                      //System.out.println("TEST overlapped: before objold "+objold);
                      //System.out.println("TEST overlapped: before objnew "+objnew);

                        //objold.setClassification(PDFAnnotation.IMG_CHOPPED);
                        objnew.addImages(objold,dx,dy);
                        objnew.setClassification(PDFAnnotation.IMG_OVERLAPPED);

                        //System.out.println("TEST overlapped: after objold "+objold);
                        //System.out.println("TEST overlapped: after objnew "+objnew);
                        //System.out.println("TEST overlapped: ================");

                        ignore = true;

                    }
                }
                if (!ignore) {
                    newtxt.add(objold);
                } else {
                    iterold.remove();
                }
            } else {
                newtxt.add(objold);
            }
        }
        annotations = newtxt;
    }


    private boolean checkOverlap(PDFAnnotation anno1, PDFAnnotation anno2) {
      // check if the objects are images
      if (!anno1.isImage() || !anno2.isImage()) {
        return false;
      }


      // do the boxes overlap?
      boolean overlap = false;
      if ( (anno1.getX() < anno2.getX() && anno1.getX() + anno1.getWidth() > anno2.getX()) ||
           (anno2.getX() < anno1.getX() && anno2.getX() + anno2.getWidth() > anno1.getX()) ||
           (anno2.getX() < anno1.getX() && anno2.getX() + anno2.getWidth() > anno1.getX() + anno1.getWidth()) ||
           (anno1.getX() < anno2.getX() && anno1.getX() + anno1.getWidth() > anno2.getX() + anno2.getWidth())
           ){

        if ( (anno1.getY() < anno2.getY() && anno1.getY() + anno1.getHeight() > anno2.getY()) ||
             (anno2.getY() < anno1.getY() && anno2.getY() + anno2.getHeight() > anno1.getY()) ||
             (anno2.getY() < anno1.getY() && anno2.getY() + anno2.getHeight() > anno1.getY() + anno1.getHeight()) ||
             (anno1.getY() < anno2.getY() && anno1.getY() + anno1.getHeight() > anno2.getY() + anno2.getHeight())
             ){
             overlap = true;
        }

      }
      if(!overlap)
        return false;


      // overlap percentage of width and overlap percentage of height
      //should be larger than dx and dy
      double percentColumn = 0.0;
      double percentRow = 0.0;
      double temp;
      if (anno1.getX() < anno2.getX() && anno1.getX() + anno1.getWidth() > anno2.getX() ) {
    	/*
    	  if(anno1.getMaxX() < anno2.getMaxX()) {
            temp = anno1.getWidth() + anno2.getWidth() - Math.abs( anno2.getX() + anno2.getWidth() - anno1.getX()) ;    		
    	}else{
        	//if(anno1.getMaxX() > anno2.getMaxX()) {
   		   temp = anno2.getWidth(); 
    	}
    	*/
        temp = anno1.getWidth() + anno2.getWidth() - Math.abs( anno2.getX() + anno2.getWidth() - anno1.getX()) ;
        if(anno1.getWidth() > anno2.getWidth())
          percentColumn = temp/(anno2.getWidth());
        else{
          percentColumn = temp/(anno1.getWidth());
        }
      }else{
        if (anno2.getX() < anno1.getX() && anno2.getX() + anno2.getWidth() > anno1.getX()) {
        	/*
        	if(anno2.getMaxX() < anno1.getMaxX()) {
                temp = anno1.getWidth() + anno2.getWidth() - Math.abs( anno1.getX() + anno1.getWidth() - anno2.getX()) ;    		
        	}else{
            	//if(anno2.getMaxX() > anno1.getMaxX()) {
       		   temp = anno1.getWidth(); 
        	}
        	*/
          temp = anno1.getWidth() + anno2.getWidth() -  Math.abs(anno1.getX() + anno1.getWidth() - anno2.getX());
          if (anno1.getWidth() > anno2.getWidth())
            percentColumn = temp / (anno2.getWidth());
          else {
            percentColumn = temp / (anno1.getWidth());
          }
        }else {
          percentColumn = 1.0; //anno1 is contained in anno2 or the other way
        }
      }
      //		///////////////////////////////////////////
      if (anno1.getY() < anno2.getY() && anno1.getY() + anno1.getHeight() > anno2.getY() ) {
        temp = anno1.getHeight() + anno2.getHeight() - Math.abs( anno2.getY() + anno2.getHeight() - anno1.getY()) ;
        if(anno1.getHeight() > anno2.getHeight())
          percentRow = temp/(anno2.getHeight());
        else{
          percentRow = temp/(anno1.getHeight());
        }
      }else{
        if (anno2.getY() < anno1.getY() && anno2.getY() + anno2.getHeight() > anno1.getY()) {
          temp = anno1.getHeight() + anno2.getHeight() -
              Math.abs(anno1.getY() + anno1.getHeight() - anno2.getY());
          if (anno1.getHeight() > anno2.getHeight())
            percentRow = temp / ( anno2.getHeight());
          else {
            percentRow = temp / ( anno1.getHeight());
            //System.out.println("ERROR: sum of heights of two boxes < 0");
            //return false;
          }
        }else {
          percentRow = 1.0; //anno1 is contained in anno2 or the other way
        }
      }


      // test
      //System.out.println("TEST: percentRow="+(percentRow*100)+", percentCol="+(percentColumn*100));
      //System.out.println("TEST: dx"+dx+", dy="+dy);

        // check the dx and dy percentage constraint
        if (  percentRow*100 > dy && percentColumn* 100 > dx){
            return true;
        }
        return false;
    }

  }
