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
package edu.illinois.ncsa.isda.imagetools.core.datatype;


import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by IntelliJ IDEA. User: kooper Date: May 2, 2005 Time: 12:49:59 PM To
 * change this template use File | Settings | File Templates.
 */
public class PDFAnnotation implements Serializable {
	private static final long serialVersionUID = 1L;

	static private Log logger = LogFactory.getLog(PDFAnnotation.class);

    static public String KEY = "Annotations";

    /**
     * flags depended on what type the information is
     */
    static final public int UNKNOWN = 0;
    static final public int IMG_CONTAINED = 3;
    static final public int IMG_CHOPPED = 4;
    static final public int IMG_OVERLAPPED = 5;
    static final public int DIM_INVALID = 7;
    static final public int AREA_INVALID = 8;
    
    private boolean duplicate = false;
    private boolean merged = false;
    private boolean invalid = false;
    private boolean partofgroup = false;
    private ArrayList proximityIndex = new ArrayList();
    
    protected int classification = 0;
    protected double x, y, w, h;
    protected Object obj;

    public PDFAnnotation(double x, double y, double w, double h, Object obj) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.obj = obj;
    }
    
    public PDFAnnotation(PDFAnnotation copy){
        this.x = copy.x;
        this.y = copy.y;
        this.w = copy.w;
        this.h = copy.h;
        this.obj = copy.obj;
   
    }

    public PDFAnnotation(Rectangle2D rect, Object obj) {
        this.x = rect.getX();
        this.y = rect.getY();
        this.w = rect.getWidth();
        this.h = rect.getHeight();
        this.obj = obj;
    }

    
    @Override
	public boolean equals(Object obj) {
    	if (!(obj instanceof PDFAnnotation)) {
    		return false;
    	}    	
    	PDFAnnotation anno = (PDFAnnotation)obj;
    	if (!anno.getBoundingBox().equals(getBoundingBox())) {
    		return false;
    	}
    	if (isImage()) {
    		if (!anno.isImage()) {
    			return false;    			
    		}
    		return ((ImageObject)this.obj).isSame((ImageObject)anno.obj);
    	}
    	return anno.obj.equals(this.obj);
	}

	public void addProximityIndex(PDFAnnotation obj) {
        proximityIndex.add(obj);
    }
    public ArrayList getProximityIndex() {
        return proximityIndex;
    }

    public boolean isText() {
        return obj instanceof String;
    }

    public boolean isImage() {
        return obj instanceof ImageObject;
    }

    public boolean isDuplicate() {
        return duplicate;
    }
    
    public boolean isMerged() {
    	return merged;
    }
    
    public boolean isInvalid() {
        return invalid;
    }

    public boolean isUnclassified() {
        return classification == UNKNOWN;
    }

    public void setDuplicate(boolean duplicate) {
        this.duplicate = duplicate;
    }
    
    public void setMerged(boolean merged) {
    	this.merged = merged;
    }

    public void setInvalid(boolean invalid) {
        this.invalid = invalid;
    }

    public int getClassification() {
        return classification;
    }

    public void setClassification(int classification) {
        this.classification = classification;
    }

    public Object getObject() {
        return obj;
    }

    public void setObject(Object obj) {
        this.obj = obj;
    }

    public boolean isPartOfGroup() {
        return partofgroup;
    }
    
    public String getText() {
        if (isText()) {
            // return the string, without the html text.
            return obj.toString().replaceAll("<[^<>]*>", "");
        }
        return "";
    }

    public void setPartOfGroup(boolean grouped) {
        partofgroup = grouped;
    }

    public Rectangle2D getBoundingBox() {
        return new Rectangle2D.Double(x, y, w, h);
    }

    public void setBoundingBox(double x, double y, double w, double h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public double getX() {
        return x;
    }

    public double getMaxX() {
        return x + w;
    }

    public double getY() {
        return y;
    }

    public double getMaxY() {
        return y + h;
    }

    public double getWidth() {
        return w;
    }

    public double getHeight() {
        return h;
    }

   
    public void drawBoundingBox(Graphics g) {
        drawBoundingBox(g, null, 0, 0);
    }

    public void drawBoundingBox(Graphics g, Color color) {
        drawBoundingBox(g, color, 0, 0);
    }

    public void drawBoundingBox(Graphics g, Color color, double dx, double dy) {
        g.setColor((color == null) ? getColor() : color);
        g.drawRect((int) (x - dx), (int) (y - dy), (int) (w + 2 * dx), (int) (h + 2 * dy));
    }

    public Color getColor() {
        if (isDuplicate() || isMerged()) {
            return Color.red;
        } else if (isInvalid()) {
            return Color.darkGray;
        } else if (isImage()) {
           switch (classification) {
             case IMG_CHOPPED:
               return Color.yellow;
             case IMG_CONTAINED:
               return Color.cyan;
             case IMG_OVERLAPPED:
               return Color.pink;
             default:
               return Color.green;
           }
        } else if (isText()) {
            return Color.blue;
        } else {
            return Color.black;
        }
    }

    public void add(PDFAnnotation add, String sep) throws IllegalArgumentException {
        if (!isText() || !add.isText()) {
            throw(new IllegalArgumentException("Both annotations have to be text."));
        }

        double[] rect = new double[4];

		// check Y first to make sure lines are added correctly
		if (getMaxY() < add.getY()) {
            obj = (String) obj + sep + (String) add.getObject();
		} else if (getY() > add.getMaxY()) {
            obj = (String) add.getObject() + sep + (String) obj;
		} else {
	        // add the text based on which text is left
	        if (getX() < add.getX()) {
	            obj = (String) obj + sep + (String) add.getObject();
	        } else {
	            obj = (String) add.getObject() + sep + (String) obj;
	        }
		}

        // find y, maximum X and maximum Y
        rect[0] = getX() < add.getX() ? getX() : add.getX();
        rect[1] = getY() < add.getY() ? getY() : add.getY();
        rect[2] = getMaxX() < add.getMaxX() ? add.getMaxX() : getMaxX();
        rect[3] = getMaxY() < add.getMaxY() ? add.getMaxY() : getMaxY();

        // change the bondingbox
        x = rect[0];
        y = rect[1];
        w = rect[2] - rect[0];
        h = rect[3] - rect[1];
    }
    // this method is designed for overlapped images
      public void addImages(PDFAnnotation add, double dx, double dy) throws IllegalArgumentException {
          if (!isImage() || !add.isImage()) {
              throw(new IllegalArgumentException("Both annotations have to be images."));
          }

          ImageObject oldObj  = (ImageObject) obj;
          ImageObject addObj = (ImageObject) add.getObject();

          double[] rect = new double[4];

          // find y, maximum X and maximum Y
          rect[0] = getX() < add.getX() ? getX() : add.getX();// minCol
          rect[1] = getY() < add.getY() ? getY() : add.getY();//minRow
          rect[2] = getMaxX() < add.getMaxX() ? add.getMaxX() : getMaxX();//maxCol
          rect[3] = getMaxY() < add.getMaxY() ? add.getMaxY() : getMaxY();//maxRow

          // change the bondingbox
          x = rect[0];
          y = rect[1];
          w = rect[2] - rect[0];
          h = rect[3] - rect[1];

          //TODO merge overlapped images ??


      }
    // this method takes the ImageObject in the argument and combines it with the
    // current ImageObject represented by PDFAnnotation if the images share at least
    // one border
    public void addChoppedImages(PDFAnnotation add, double dx, double dy) throws IllegalArgumentException {
        if (!isImage() || !add.isImage()) {
            throw(new IllegalArgumentException("Both annotations have to be images."));
        }

        ImageObject oldObj  = (ImageObject) obj;
        ImageObject addObj = (ImageObject) add.getObject();

        double[] rect = new double[4];

        // find y, maximum X and maximum Y
        rect[0] = getX() < add.getX() ? getX() : add.getX();// minCol
        rect[1] = getY() < add.getY() ? getY() : add.getY();//minRow
        rect[2] = getMaxX() < add.getMaxX() ? add.getMaxX() : getMaxX();//maxCol
        rect[3] = getMaxY() < add.getMaxY() ? add.getMaxY() : getMaxY();//maxRow

/*
        int numrows = (int) (rect[3] - rect[1] +1);
        int numcols = (int) (rect[2] - rect[0] +1);
        // sanity checks
        if ( numrows <= 0 || numcols <= 0){
          logger.error("ERROR: image dimensions are <=0 ; numrows="+numrows+", numcols="+numcols);
          for(int i=0;i<4;i++){
            System.err.println("ERROR: rect["+i+"]="+rect[i]);
          }
          return;
        }
        if ( numrows < oldObj.getNumRows() || numrows < addObj.getNumRows()){
          System.err.println("ERROR: numrows of merged images are larger than the newObj numrows ; numrows="+numrows+
                       ", numrowsOld="+oldObj.getNumRows()+", numrowsAdd="+addObj.getNumRows());
          for(int i=0;i<4;i++){
            System.err.println("ERROR: rect["+i+"]="+rect[i]);
          }
          System.err.println("ERROR: oldObj="+oldObj);
          System.err.println("ERROR: addObj="+addObj);
          //return;
          numrows = oldObj.getNumRows() < addObj.getNumRows() ? addObj.getNumRows() : oldObj.getNumRows();
        }
        if ( numcols < oldObj.getNumCols() || numcols < addObj.getNumCols()){
          System.err.println("ERROR: numcols of merged images are larger than the newObj numcols ; numcols="+numcols+
                       ", numcolsOld="+oldObj.getNumCols()+", numcolsAdd="+addObj.getNumCols());
          for(int i=0;i<4;i++){
            System.err.println("ERROR: rect["+i+"]="+rect[i]);
          }
          System.err.println("ERROR: oldObj="+oldObj);
          System.err.println("ERROR: addObj="+addObj);
          //return;
          numcols = oldObj.getNumCols() < addObj.getNumCols() ? addObj.getNumCols() : oldObj.getNumCols();
        }
*/
        int b = 1;
        if( oldObj.getNumBands() < addObj.getNumBands() )
          b = addObj.getNumBands();
        else
          b = oldObj.getNumBands();

        String type = "DOUBLE";
        if ( oldObj.getType() == addObj.getType() )
          type = oldObj.getTypeString();
/*
        ImageObject newObj = null;
        try{
            newObj = ImageObject.createImage( numrows, numcols,b, type);
        }catch(Exception e){
          logger.error("ERROR: could not allocate memory for newObj "+e);
          return;
        }
 */
/*
//test
        System.out.println("TEST: newObj created ="+newObj);
        logger.error("TEST: newObj numcols ; numcols="+numcols+
                        ", numcolsOld="+oldObj.getNumCols()+", numcolsAdd="+addObj.getNumCols());
        logger.error("TEST: newObj numrows ; numrows="+numrows+
                           ", numrowsOld="+oldObj.getNumRows()+", numrowsAdd="+addObj.getNumRows());
         for(int i=0;i<4;i++){
           System.err.println("ERROR: rect["+i+"]="+rect[i]);
         }
////////////////////
  */
       int numrows, numcols;
        ImageObject newObj = null;

        if ( Math.abs(getX() - add.getX() )<=dx || Math.abs(getX()+getWidth() - (add.getX()+add.getWidth()) )<=dx ){
          numrows = oldObj.getNumRows() + addObj.getNumRows();
          numcols = oldObj.getNumCols() < addObj.getNumCols() ? addObj.getNumCols() : oldObj.getNumCols();// maxCol
          try{
            newObj = ImageObject.createImage( numrows, numcols,b, type);
          }catch(Exception e){
            logger.error("ERROR: could not allocate memory for newObj "+e);
            return;
          }
        }else{
          numrows = oldObj.getNumRows() < addObj.getNumRows() ? addObj.getNumRows() : oldObj.getNumRows();// maxRow
          numcols = oldObj.getNumCols() + addObj.getNumCols();
          try{
            newObj = ImageObject.createImage( numrows, numcols,b, type);
          }catch(Exception e){
            logger.error("ERROR: could not allocate memory for newObj "+e);
            return;
          }

        }

        int insertRow, insertCol;
        boolean done = false;
       // double dx = 1.0;
       // double dy = 1.0;

        ////////////////////left column
        //below of image1 aligned with left column
        if ( Math.abs(getX() - add.getX() )<=dx && Math.abs(getY() + getHeight() - add.getY() )<=dy ){

          insertRow = 0;
          insertCol = 0;
          insertImages(newObj, oldObj, insertRow, insertCol);

          insertRow = oldObj.getNumRows();
          insertCol = 0;
          insertImages(newObj, addObj, insertRow, insertCol);
          done = true;
        }
        //above of image1 aligned with left column
        if ( !done && Math.abs(getX() - add.getX()) <= dx
             && Math.abs(getY()- (add.getY() + add.getHeight() ) ) <= dy  ) {
          insertRow = 0;
          insertCol = 0;
          insertImages(newObj, addObj, insertRow, insertCol);

          insertRow = addObj.getNumRows();
          insertCol = 0;
          insertImages(newObj, oldObj, insertRow, insertCol);
          done = true;
        }
        ////////////////////right column
        //below of image1 aligned with right column
        if ( !done && Math.abs(getX()+getWidth() - (add.getX()+add.getWidth()) )<=dx
             && Math.abs(getY() - add.getY() )<=dy ){
          insertRow = 0;
          insertCol = 0;
          insertImages(newObj, oldObj, insertRow, insertCol);

          insertRow = newObj.getNumRows() - addObj.getNumRows();
          insertCol = newObj.getNumCols() - addObj.getNumCols();;
          insertImages(newObj, addObj, insertRow, insertCol);
          done = true;
        }
        //above of image1 aligned with right column
        if ( !done && Math.abs(getX()+getWidth() - (add.getX()+add.getWidth()) )<=dx
             && Math.abs(getY() - (add.getY() + add.getHeight())  )<=dy ){
          insertRow = 0;
          insertCol = newObj.getNumCols() - addObj.getNumCols();
          insertImages(newObj, addObj, insertRow, insertCol);

          insertRow = addObj.getNumRows();
          insertCol = newObj.getNumCols() - oldObj.getNumCols();;
          insertImages(newObj, oldObj, insertRow, insertCol);
          done = true;
        }
        ////////////////////left side of image1
        //upper left of image1
        if ( !done && Math.abs(getX() - (add.getX()+add.getWidth()) )<=dx
             && Math.abs(getY() - add.getY() )<=dy ){
          insertRow = 0;
          insertCol = 0;
          insertImages(newObj, addObj, insertRow, insertCol);

          insertRow = 0;
          insertCol = addObj.getNumCols();
          insertImages(newObj, oldObj, insertRow, insertCol);
          done = true;
        }
        //lower left of image1
        if ( !done && Math.abs(getX() - (add.getX()+add.getWidth()) )<=dx
             && Math.abs(getY() + getHeight() - (add.getY() + add.getHeight()) )<=dy   ){
          insertRow = newObj.getNumRows() - addObj.getNumRows();
          insertCol = 0;
          insertImages(newObj, addObj, insertRow, insertCol);

          insertRow = 0;
          insertCol = newObj.getNumCols() - oldObj.getNumCols();;
          insertImages(newObj, oldObj, insertRow, insertCol);
          done = true;
        }
        ////////////////////right side of image1
        //upper right of image1
        if ( !done && Math.abs(getX()+ getWidth() - add.getX())<=dx
             && Math.abs(getY() - add.getY() )<=dy ){
          insertRow = 0;
          insertCol = 0;
          insertImages(newObj, oldObj, insertRow, insertCol);

          insertRow = 0;
          insertCol = oldObj.getNumCols();
          insertImages(newObj, addObj, insertRow, insertCol);
          done = true;
        }
        //lower right of image1
        if ( !done && Math.abs(getX() +getWidth() - add.getX())<=dx
             && Math.abs(getY() + getHeight() - (add.getY() + add.getHeight())  )<=dy ){
          insertRow = 0;
          insertCol = 0;
          insertImages(newObj, oldObj, insertRow, insertCol);

          insertRow = newObj.getNumCols() - addObj.getNumCols();
          insertCol = oldObj.getNumCols();;
          insertImages(newObj, addObj, insertRow, insertCol);
          done = true;
        }
        //test
        if(!done){
          System.out.println("ERROR: none of the pair-wise configuration was applied");
          return;
        }

        // change the bondingbox and assign the obj
        x = rect[0];
        y = rect[1];
        w = rect[2] - rect[0];
        h = rect[3] - rect[1];
        obj = (ImageObject)newObj;

        return;
    }
    // the code that takes any addObj image and inserts it into a specified (insertRow, InsertCol) locations
    // inside of newObj image
    private void insertImages(ImageObject newObj, ImageObject addObj, int insertRow, int insertCol){

      int indexNew, index, row, col,band;
      int offsetNew;

      index = 0;
      indexNew = (insertRow* newObj.getNumCols() + insertCol)*newObj.getNumBands();
      offsetNew = (newObj.getNumCols()-addObj.getNumCols() )*newObj.getNumBands();
      for(row=insertRow;row< newObj.getNumRows() && row-insertRow< addObj.getNumRows() ;row++){
        for (col = insertCol;
             col < newObj.getNumCols() && col - insertCol < addObj.getNumCols();
             col++) {
          for (band = 0;
               band < newObj.getNumBands() && band < addObj.getNumBands(); band++) {
            //test
  /*          if(indexNew < 0 || index < 0 || indexNew >= newObj.getSize() || index >= addObj.getSize() ){
              System.err.println("ERROR: indexNew out of bounds = indexNew = "+indexNew +">="+newObj.getSize());
              System.err.println("ERROR: index out of bounds = index = "+index +">="+addObj.getSize());

              System.err.println("ERROR: row = "+row+", col="+col+",band="+band);
              System.err.println("ERROR: newObj="+newObj);
              System.err.println("ERROR: addObj="+addObj);
            }
      */
            newObj.set(indexNew, addObj.getDouble(index));
            index++;
            indexNew++;
          }
          indexNew += (newObj.getNumBands() - addObj.getNumBands());
        }
        indexNew += offsetNew;
      }
      return;
    }


    public String toString() {
        String result =  "[x=" + x + ", y=" + y + ", w=" + w + ", h=" + h + "]\n\t";
        if (isImage()) {
            result += "IMAGE : "+ obj.toString();
        } else {
            result += "TEXT ";
            if (isDuplicate()) {
                result += "(duplicate) ";
            }
            else if (isMerged()) {
            	result += "(merged) ";
            }
            result += obj.toString();
        }

        return result;

    }

	public PDFFont getFont() {
		
		int size=0;		
		String face="";
		PDFFont font=null;
		
		if(this.isText()){
			String results=this.toString();
			String temp="";
			
			StringTokenizer T=new StringTokenizer(results);
			temp=T.nextToken();
			try{
				while(!temp.equalsIgnoreCase("><font"))			
					temp=T.nextToken();
				temp=T.nextToken();
				face=temp.substring(6,temp.length()-1);
				temp=T.nextToken();
				
			if(temp.substring(18,19).equals("p"))	
				size=Integer.parseInt(temp.substring(17, 18));
			else
			
				size=Integer.parseInt(temp.substring(17, 19));

				
			font=new PDFFont(size,face);
			
		}
		catch(Exception e){
			font=new PDFFont(size,face);
			
			
		}
	}
		return font;
	
	}
}


