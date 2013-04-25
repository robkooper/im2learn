package edu.illinois.ncsa.isda.im2learn.ext.misc;

/*

 * Morpho.java

 *

 */



import java.io.*;

import java.lang.Math;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageException;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.datatype.LimitValues;
import edu.illinois.ncsa.isda.im2learn.ext.math.GeomOper;


/** this class is not public !!!!!!



@author  Peter Bajcsy

@version 1.0


Ported from I2K by Tenzing Shaw
*/



class MorphoObject{



   // data related values

   protected float _minVal;

   protected float _maxVal;

   ImageObject _morphElem=null; // structure element

   //protected int _sizeElem; // size of structure element

  // 0 is off and 1 is on

   //protected int _kernel;

   //protected int _rowSize, _colSize;

   protected int _halfRow,_halfCol,_halfRow1,_halfCol1;

   protected String _edgeType;



   ImageObject _resObject = null;

   ImageObject _edgeMap = null;

   // classes

   LimitValues _lim = new LimitValues();

   //GeomObject _myGeom = new GeomObject();

   GeomOper _myGeom = new GeomOper();



   public MorphoObject(){

        // default values

	ResetMorphoObject();

    }

    public void ResetMorphoObject(){

        // default values

        _minVal = 0.0F;

        _maxVal = 255.0F;



        _halfRow = _halfCol = -1;

        _halfRow1 = _halfCol1 = -1;

	_edgeType = "DILATE";



	_resObject = null;

        _edgeMap = null;

    }



    //Getters

    // Data values

    public float   GetMorphoMinVal()      {return _minVal;}

    public float   GetMorphoMaxVal()      {return _maxVal;}



    public boolean SetEdgeType(String val){

      if(val != null && (val.equalsIgnoreCase("DILATE")  || val.equalsIgnoreCase("ERODE")) ){

          _edgeType = val;

           return true;

       }else{

        System.out.println("ERROR: allowed String val = DILATE or ERODE");

         return false;

      }

    }

    public String  GetEdgeType()      {return _edgeType;}



    public boolean SetMorphElem(ImageObject im) throws CloneNotSupportedException{

     if((im == null) || (im.getNumRows() <= 0) || (im.getNumCols() <= 0)){

        System.out.println("ERROR: there is no data in input ImageObject");

        return false;

     }

     _morphElem = null;

     _morphElem = (ImageObject) im.clone();

     //_sizeElem = sizeElem;

     return true;

   }



    public ImageObject GetMorphElem(){

      return _morphElem;

    }



    public ImageObject GetImageObject(){

	return _resObject;

    }

    public ImageObject GetEdgeMap(){

	return _edgeMap;

    }

    public boolean CreateMorphElem(int rowSize, int colSize) throws ImageException{

        if(rowSize < 1 || colSize < 1){

          System.out.println("ERROR: input size of structuring element should be larger than 1");

          return false;

        }



        if(_morphElem == null || rowSize != _morphElem.getNumRows() || colSize != _morphElem.getNumCols())

          _morphElem = ImageObject.createImage(rowSize, colSize, 1, ImageObject.TYPE_BYTE);



        for(int i=0;i<_morphElem.getSize();i++)

           _morphElem.setByte(i, _lim.BIN_ONE);



        return true;

    }

    public int GetMorphElemNumrows(){

       if(_morphElem == null)

         return -1;

       return   _morphElem.getNumRows();

    }

    public int GetMorphElemNumcols(){

       if(_morphElem == null)

         return -1;

       return   _morphElem.getNumCols();

    }



}



/**

<B>

The class Morpho provides a tool for applying morphological operator on two-dimensional

multivariate (multi-band) images.

</B>

<BR>

<BR>

<B>Description:</B> A morphological opening and closing is computed by clicking.

<BR>

<BR>



@author  Peter Bajcsy

@version 1.0



*/





////////////////////////////////////////////////////////////////////

// this class performs morphological computation

////////////////////////////////////////////////////////////////////

public class Morpho extends MorphoObject{



    private boolean _debugMorpho;



    //constructor

    public Morpho(){

	_debugMorpho = true;

    }

   public Morpho(int rowSize, int colSize) throws ImageException{

        // default values

	// ResetMorphoObject();

        if( !CreateMorphElem(rowSize, colSize) ){

          System.out.println("ERROR: could not create morphological element");

        }

    }



    // Getters

    public MorphoObject GetMorphoObject(){

	return (MorphoObject)this;

    }

    //Setters

    public void SetDebugFlag(boolean flag){

	_debugMorpho = flag;

    }



    //////////////////////////////////////////////////////////////

    // doers

    // Morphological  computation

   //////////////////////////////////////////////////////////////////////////

 /*

    // performs dilation over a binary image with a structure element

    // morphElem image (0 - not defined, other  defined)

    //////////////////////////////////////////////////////////////////////////

    public boolean DilateBinImage(ImageObject im){

       //sanity check

      if(_morphElem==null){

        System.out.println("ERROR: no structure element to perform morphological operation");

        return false;

      }



      if(im.sampPerPixel != 1 || im.sampType.equalsIgnoreCase("BYTE") == false){

        System.out.println("ERROR: input image should be binary one band image");

        return false;

      }



      // allocate memory only if necessary

      if(_resObject == null || im.numrows != _resObject.numrows  || im.numcols != _resObject.numcols

         || im.sampPerPixel != _resObject.sampPerPixel || _resObject.image == null ){

        ImageObject _resObject = new ImageObject();

        _resObject.numrows = im.numrows;

        _resObject.numcols = im.numcols;

        //im.sampPerPixel; there will be the same structure element

        // applied in every band

        _resObject.sampPerPixel = 1;

        _resObject.size = _resObject.numrows * _resObject.numcols;

        _resObject.sampType = "BYTE"; //im.sampType;

        _resObject.image = new byte[(int)_resObject.size];

      }





      _halfRow = _morphElem.numrows >> 1;

      _halfCol = _morphElem.numcols >> 1;

      int shiftIm = (_halfRow*im.numcols + _halfCol)*im.sampPerPixel;

      boolean signal;

      int i,j,m1,n1;

      int idx1,idxElem,idx=0;

      int oneRow = im.numcols * im.sampPerPixel;

      for(i=0;i<im.numrows;i++){

        for(j=0;j<im.numcols;j++){

          _resObject.image[idx] = im.image[idx];

          //non zeros are not dilated

          if(im.image[idx] != _lim.BIN_ZERO){

            idx++;

            continue;

          }

          signal = true;

          idxElem=0;

          idx1 = idx - shiftIm;

          for(m1=0;m1<_morphElem.numrows && signal;m1++){

            for(n1=0;n1<_morphElem.numcols && signal;n1++){

              if(_morphElem.image[idxElem] == _lim.BIN_ONE )  {

	        if(((i-__halfRow+m1)>=0) && ((j-_halfCol+n1)>=0)

                && ((i-__halfRow+m1)<im.numrows) && ((j-_halfCol+n1)<im.numcols))

       	          if( im.image[idx1] == _lim.BIN_ONE)

	            signal =false;

	      }

              if(!signal)

                _resObject.image[idx] = _lim.BIN_ONE; //1;



              idx1+=im.sampPerPixel;

              idxElem++;

            }// end n1

	    idx1+=oneRow - _morphElem.numcols;

          }// end m1

          idx+=_resObject.sampPerPixel;



       } // end j

     }// end i

     return true;

   }



    //////////////////////////////////////////////////////////////////////////

   // performs erosion over a binary image with a structure element

    // morphElem image (0 - not defined, other  defined)

    //////////////////////////////////////////////////////////////////////////

    public boolean ErodeBinImage(ImageObject im){

       //sanity check

      if( _morphElem==null || _morphElem.image == null){

        System.out.println("ERROR: no structure element to perform morphological operation");

        return false;

      }



      if(im.sampPerPixel != 1 || im.sampType.equalsIgnoreCase("BYTE") == false){

        System.out.println("ERROR: input image should be binary (BYTE type) one band image");

        return false;

      }



      // allocate memory only if necessary

      if(_resObject == null || im.numrows != _resObject.numrows || im.numcols != _resObject.numcols

         || im.sampPerPixel != _resObject.sampPerPixel || _resObject.image == null ){

        _resObject = new ImageObject();

        _resObject.numrows = im.numrows;

        _resObject.numcols = im.numcols;

        //im.sampPerPixel; there will be the same structure element

        // applied in every band

        _resObject.sampPerPixel = 1;

        _resObject.size = _resObject.numrows * _resObject.numcols;

        _resObject.sampType = "BYTE";

        _resObject.image = new byte[(int)_resObject.size];

      }



      _halfRow = _morphElem.numrows >> 1;

      _halfCol = _morphElem.numcols >> 1;

      int shiftIm = (_halfRow*im.numcols + _halfCol)*im.sampPerPixel;

      boolean signal;

      int i,j,m1,n1;

      int idx1,idxElem,idx=0;

      int oneRow = im.numcols * im.sampPerPixel;

      for(i=0;i<im.numrows;i++){

        for(j=0;j<im.numcols;j++){

          _resObject.image[idx] = im.image[idx];

          //zeros are not eroded

          if(im.image[idx] != _lim.BIN_ONE){

            idx++;

            continue;

          }

          signal = true;

          idxElem=0;

          idx1 = idx - shiftIm;

          for(m1=0;m1<_morphElem.numrows && signal;m1++){

            for(n1=0;n1<_morphElem.numcols && signal;n1++){

              if(_morphElem.image[idxElem] == _lim.BIN_ONE )  {

	        if(((i-_halfRow+m1)>=0) && ((j-_halfCol+n1)>=0)

                && ((i-_halfRow+m1)<im.numrows) && ((j-_halfCol+n1)<im.numcols))

       	          if( im.image[idx1] == _lim.BIN_ZERO)

	            signal =false;

	       }

               if(!signal)

                _resObject.image[idx] = _lim.BIN_ZERO; //0;



               idx1+=im.sampPerPixel;

               idxElem++;

             }// end n1

	     idx1+=oneRow - _morphElem.numcols;

          }// end m1

          idx+=_resObject.sampPerPixel;



        } // end j

      }//end i

      return true;



    }

*/

   private boolean SanityInput(ImageObject im) throws ImageException{

          //sanity check

      if(im.getType() != ImageObject.TYPE_BYTE && im.getType() != ImageObject.TYPE_SHORT){

        System.out.println("ERROR: input image other that BYTE or SHORT is not supported");

        return false;

      }



      if(im.getType() == ImageObject.TYPE_BYTE && im == null){

        System.out.println("ERROR: input image BYTE is null");

        return false;

      }

      if(im.getType() == ImageObject.TYPE_SHORT && im == null){

        System.out.println("ERROR: input image SHORT is null");

        return false;

      }

      if(_morphElem==null || (_morphElem.getNumRows() <= 0) || (_morphElem.getNumCols() <= 0)){

        System.out.println("Warning: no structure element to perform morphological operation");

        System.out.println("Warning: setting the operator to 3x3");

        _morphElem = ImageObject.createImage(3, 3, 1, ImageObject.TYPE_BYTE);

        _morphElem.setSize(3,3,1);

        for(int k = 0; k < _morphElem.getSize(); k++)

          _morphElem.setByte(k, _lim.BIN_ONE);



      }



      // allocate memory only if necessary

      if(_resObject == null || im.getNumRows() != _resObject.getNumRows()  || im.getNumCols() != _resObject.getNumCols()

         || im.getNumBands() != _resObject.getNumBands() || im.getType() != _resObject.getType() ){

        _resObject = ImageObject.createImage(im.getNumRows(), im.getNumCols(), im.getNumBands(), im.getType());

        _resObject.setSize(im.getNumCols(),im.getNumRows(),im.getNumBands());

      }





      _halfRow = (_morphElem.getNumRows()-1) >> 1;

      if( (_halfRow << 1) != (_morphElem.getNumRows()-1))

         _halfRow ++;



      _halfCol = (_morphElem.getNumCols()-1) >> 1;

      if( (_halfCol << 1) != (_morphElem.getNumCols()-1))

         _halfCol ++;

      _halfRow1 = _morphElem.getNumRows() - _halfRow;

      _halfCol1 = _morphElem.getNumCols() - _halfCol;

      //test

      //System.out.println("Test; _halfRow = " + _halfRow+" _halfCol="+_halfCol);

      //System.out.println("Test; _halfRow1 = " + _halfRow1+" _halfCol1="+_halfCol1);

      return true;

  }


  // Sang-Chul

  public boolean MorphOpen(ImageObject im) throws CloneNotSupportedException, ImageException {
    //sanity check

   if ( !SanityInput(im) ){
     return false;
   }

   if(!Erode(im))
     return false;

   if(!Dilate((ImageObject) GetImageObject().clone()))
     return false;

   return true;
  }



  // Sang-Chul

   public boolean MorphClose(ImageObject im) throws CloneNotSupportedException, ImageException {
     //sanity check

    if ( !SanityInput(im) ){
      return false;
    }

    if(!Dilate(im))
      return false;

    if(!Erode((ImageObject) GetImageObject().clone()))
      return false;

    return true;
  }

    //////////////////////////////////////////////////////////////////////////

    // performs dilation given an image and a structure element

    // morphElem image (0 - not defined, other  defined)

    //////////////////////////////////////////////////////////////////////////

    public boolean Dilate(ImageObject im) throws ImageException{

       //sanity check

      if ( !SanityInput(im) ){

        return false;

      }



      int shiftIm = (_halfRow*im.getNumCols() + _halfCol)*im.getNumBands();

      int shiftImRow = _morphElem.getNumCols()*im.getNumBands();

      boolean signal;

      int i,j,k,m1,n1;

      int idx1,idxElem,idxIm=0;

      int oneRow = im.getNumCols() * im.getNumBands();

      idxIm = shiftIm;



      if(im.getType()==ImageObject.TYPE_BYTE){

        int [] maxValue;

        maxValue = new int[im.getNumBands()];

        int val;



        for(i=_halfRow;i<im.getNumRows()-_halfRow1;i++){

          for(j=_halfCol;j<im.getNumCols()-_halfCol1;j++){

            // init max values

            for(k=0;k<im.getNumBands();k++){

                val = im.getByte(idxIm+k);

                if(val < 0 )

                   val += _lim.MAXPOS_BYTE; //256

                maxValue[k] = val;

                //maxValue[k] = im.image[idxIm+k];

            }

            //test

            //if(i<_halfRow+2 && j == im.numcols-_halfCol1-1){

            //  System.out.println("Test:idxIm = " +idxIm + " k="+k+" i="+i+" j="+j);

            //  System.out.println("Test: idxImComputed =" + ((int)(i*im.numcols+j)*im.sampPerPixel) );

            //}

            // compute dilated values at all bands

            for(k=0;k<im.getNumBands();k++){

              idx1 = idxIm - shiftIm + k;

              idxElem = 0;

              for(m1=0;m1< _morphElem.getNumRows() ;m1++){

	         for(n1=0;n1< _morphElem.getNumCols() ;n1++){

     	           if(_morphElem.getByte(idxElem) == _lim.BIN_ONE )  {

                      val = im.getByte(idx1);

                       if(val < 0 )

                         val += _lim.MAXPOS_BYTE; //256

                       if( val > maxValue[k] ){

                           maxValue[k] = val;

  	               }

	                // if( im.image[idx1] > maxValue[k] ){

                        //   maxValue[k] = im.image[idx1];

	                // }

	           }

	           idxElem++;

                   idx1+=im.getNumBands();

                 }// end n1

                 idx1= idx1-shiftImRow+oneRow;

               }// end m1



                if(maxValue[k] <= _lim.MAX_BYTE)

                  _resObject.setByte(idxIm+k,(byte)maxValue[k]);

                else

                  _resObject.setByte(idxIm+k,(byte)(maxValue[k]- _lim.MAXPOS_BYTE));



               //for(k=0;k<im.sampPerPixel;k++){

               //  _resObject.image[idxIm+k] = maxValue[k];

               //}



             }// end k

             idxIm+=im.getNumBands();

          }// end j

          idxIm += shiftImRow;

        } // end i



        // take care of the borders

        // top rim

        for(i=0;i<shiftIm;i++)

          _resObject.setByte(i,im.getByte(i));



        // bottom rim

        shiftIm = (_halfRow1*im.getNumCols()+_halfCol1)*im.getNumBands();

        for(i=(int)im.getSize()-shiftIm;i<im.getSize();i++)

           _resObject.setByte(i,im.getByte(i));



        //first col

        shiftIm = _halfCol*im.getNumBands();

        for(idx1=0;idx1< im.getSize();idx1 +=oneRow){

          for(i=0;i<shiftIm;i++)

             _resObject.setByte(idx1+i,im.getByte(idx1+i));

        }

        //last col

        shiftIm = _halfCol1*im.getNumBands();

        for(idx1=oneRow-shiftIm;idx1<im.getSize();idx1 += oneRow){

          for(i=0;i<_halfCol1;i++)

             _resObject.setByte(idx1+i,im.getByte(idx1+i));

        }





      }// end of BYTE

      if(im.getType()==ImageObject.TYPE_SHORT) {

        int [] maxValue;

        maxValue = new int[im.getNumBands()];

        int val;



        for(i=_halfRow;i<im.getNumRows()-_halfRow1;i++){

          for(j=_halfCol;j<im.getNumCols()-_halfCol1;j++){

            // init max values

            for(k=0;k<im.getNumBands();k++){

                val = im.getShort(idxIm+k);

                if(val < 0 )

                   val += _lim.MAXPOS_SHORT;

                maxValue[k] = val;



                //maxValue[k] = im.imageShort[idxIm+k];

            }

            // compute dilated values at all bands

            for(k=0;k<im.getNumBands();k++){

              idx1 = idxIm - shiftIm + k;

              idxElem = 0;

              for(m1=0;m1< _morphElem.getNumRows() ;m1++){

	         for(n1=0;n1< _morphElem.getNumCols() ;n1++){

     	           if(_morphElem.getShort(idxElem) == _lim.BIN_ONE )  {

                      val = im.getShort(idx1);

                       if(val < 0 )

                         val += _lim.MAXPOS_SHORT;

                       if( val > maxValue[k] ){

                           maxValue[k] = val;

  	               }



	               //  if( im.imageShort[idx1] > maxValue[k] ){

                       //    maxValue[k] = im.imageShort[idx1];

	               //  }

	           }

	           idxElem++;

                   idx1+=im.getNumBands();

                 }// end n1

                 idx1= idx1-shiftImRow+oneRow;

               }// end m1



                if(maxValue[k] <= _lim.MAX_BYTE)

                  _resObject.setShort(idxIm+k,(short)maxValue[k]);

                else

                  _resObject.setShort(idxIm+k,(short)(maxValue[k]- _lim.MAXPOS_SHORT));



               //_resObject.imageShort[idxIm+k] = maxValue[k];

               //for(k=0;k<im.sampPerPixel;k++){

               //  _resObject.imageShort[idxIm+k] = maxValue[k];

               //}



             }// end k

             idxIm+=im.getNumBands();

          }// end j

          idxIm += shiftImRow;

        } // end i



        // take care of the borders

        // top rim

        for(i=0;i<shiftIm;i++)

          _resObject.setShort(i,im.getShort(i));



        // bottom rim

        shiftIm = (_halfRow1*im.getNumCols()+_halfCol1)*im.getNumBands();

        for(i=(int)im.getSize()-shiftIm;i<im.getSize();i++)

           _resObject.setShort(i,im.getShort(i));



        //first col

        shiftIm = _halfCol*im.getNumBands();

        for(idx1=0;idx1< im.getSize();idx1 +=oneRow){

          for(i=0;i<shiftIm;i++)

             _resObject.setShort(idx1+i,im.getShort(idx1+i));

        }

        //last col

        shiftIm = _halfCol1*im.getNumBands();

        for(idx1=oneRow-shiftIm;idx1<im.getSize();idx1 += oneRow){

          for(i=0;i<_halfCol1;i++)

             _resObject.setShort(idx1+i,im.getShort(idx1+i));

        }





      }// end of SHORT



     // compute euclidean between dilated vector and original vector

     // if you would like to compute edge map

     return true;

   }



    //////////////////////////////////////////////////////////////////////////

    // performs erosion given an image and a structure element

    // morphElem image (0 - not defined, other  defined)

    //////////////////////////////////////////////////////////////////////////

    public boolean Erode(ImageObject im) throws ImageException{

       //sanity check

      if ( !SanityInput(im) ){

        return false;

      }



      int shiftIm = (_halfRow*im.getNumCols() + _halfCol)*im.getNumBands();

      int shiftImRow = _morphElem.getNumCols()*im.getNumBands();

      boolean signal;

      int i,j,k,m1,n1;

      int idx1,idxElem,idxIm=0;

      int oneRow = im.getNumCols() * im.getNumBands();



      idxIm = shiftIm;

      //System.out.println("Test; idxIm = " +idxIm);



      if(im.getType() == ImageObject.TYPE_BYTE ){

        int [] minValue;

        minValue = new int[im.getNumBands()];

        int val;



        for(i=_halfRow;i<im.getNumRows()-_halfRow1;i++){

          for(j=_halfCol;j<im.getNumCols()-_halfCol1;j++){

            // init min values

            for(k=0;k<im.getNumBands();k++){

                val = im.getByte(idxIm+k);

                if(val < 0 ){

                   minValue[k] = val+_lim.MAXPOS_BYTE;//256;

                }else{

                   minValue[k] = val;

                }



                //minValue[k] = im.image[idxIm+k];

            }

            // compute eroded values at all bands

            for(k=0;k<im.getNumBands();k++){

              idx1 = idxIm - shiftIm + k;

              idxElem = 0;

              for(m1=0;m1< _morphElem.getNumRows() ;m1++){

	         for(n1=0;n1< _morphElem.getNumCols() ;n1++){

     	           if(_morphElem.getByte(idxElem) == _lim.BIN_ONE )  {

                       val = im.getByte(idx1);

                       if(val < 0 )

                         val += _lim.MAXPOS_BYTE; // 256;

                       if( val < minValue[k] ){

                           minValue[k] = val;

  	               }



                       //if( im.image[idx1] < minValue[k] ){

                       //   minValue[k] = im.image[idx1];

  	               // }

	           }

	           idxElem++;

                   idx1+=im.getNumBands();

                 }// end n1

                 idx1= idx1-shiftImRow+oneRow;

               }// end m1



                if(minValue[k] <= _lim.MAX_BYTE)

                  _resObject.setByte(idxIm+k,(byte)minValue[k]);

                else

                  _resObject.setByte(idxIm+k, (byte)(minValue[k]-_lim.MAXPOS_BYTE));



               //_resObject.image[idxIm+k] = minValue[k];

               //for(k=0;k<im.sampPerPixel;k++){

               //  _resObject.image[idxIm+k] = minValue[k];

               //}



             }// end k

             idxIm+=im.getNumBands();

          }// end j

          idxIm += shiftImRow;

        } // end i



        // take care of the borders

        // top rim

        for(i=0;i<shiftIm;i++)

          _resObject.setByte(i,im.getByte(i));



        // bottom rim

        shiftIm = (_halfRow1*im.getNumCols()+_halfCol1)*im.getNumBands();

        for(i=(int)im.getSize()-shiftIm;i<im.getSize();i++)

           _resObject.setByte(i,im.getByte(i));



        //first col

        shiftIm = _halfCol*im.getNumBands();

        for(idx1=0;idx1< im.getSize();idx1 +=oneRow){

          for(i=0;i<shiftIm;i++)

             _resObject.setByte(idx1+i,im.getByte(idx1+i));

        }

        //last col

        shiftIm = _halfCol1*im.getNumBands();

        for(idx1=oneRow-shiftIm;idx1<im.getSize();idx1 += oneRow){

          for(i=0;i<_halfCol1;i++)

             _resObject.setByte(idx1+i,im.getByte(idx1+i));

        }





      }// end of BYTE

      if(im.getType() == ImageObject.TYPE_SHORT){

        int [] minValue;

        minValue = new int[im.getNumBands()];

        int val;



        for(i=_halfRow;i<im.getNumRows()-_halfRow1;i++){

          for(j=_halfCol;j<im.getNumCols()-_halfCol1;j++){

            // init min values

            for(k=0;k<im.getNumBands();k++){

                val = im.getShort(idxIm+k);

                if(val < 0 ){

                   minValue[k] = val+_lim.MAXPOS_SHORT;

                }else{

                   minValue[k] = val;

                }



                //minValue[k] = im.imageShort[idxIm+k];

            }

            // compute eroded values at all bands

            for(k=0;k<im.getNumBands();k++){

              idx1 = idxIm - shiftIm + k;

              idxElem = 0;

              for(m1=0;m1< _morphElem.getNumRows() ;m1++){

	         for(n1=0;n1< _morphElem.getNumCols() ;n1++){

     	           if(_morphElem.getShort(idxElem) == _lim.BIN_ONE )  {

                       val = im.getShort(idx1);

                       if(val < 0 )

                         val += _lim.MAXPOS_SHORT;

                       if( val < minValue[k] ){

                           minValue[k] = val;

  	               }



                      //if( im.imageShort[idx1] < minValue[k] ){

                      //  minValue[k] = im.imageShort[idx1];

	             //}

	           }

	           idxElem++;

                   idx1+=im.getNumBands();

                 }// end n1

                 idx1= idx1-shiftImRow+oneRow;

               }// end m1



                if(minValue[k] <= _lim.MAX_SHORT)

                  _resObject.setShort(idxIm+k,(short)minValue[k]);

                else

                  _resObject.setShort(idxIm+k, (short)(minValue[k]- _lim.MAXPOS_SHORT));



               //_resObject.imageShort[idxIm+k] = minValue[k];

               //for(k=0;k<im.sampPerPixel;k++){

               //  _resObject.imageShort[idxIm+k] = minValue[k];

               //}



             }// end k

             idxIm+=im.getNumBands();

          }// end j

          idxIm += shiftImRow;

        } // end i



        // take care of the borders

        // top rim

        for(i=0;i<shiftIm;i++)

          _resObject.setShort(i,im.getShort(i));



        // bottom rim

        shiftIm = (_halfRow1*im.getNumCols()+_halfCol1)*im.getNumBands();

        for(i=(int)im.getSize()-shiftIm;i<im.getSize();i++)

           _resObject.setShort(i,im.getShort(i));



        //first col

        shiftIm = _halfCol*im.getNumBands();

        for(idx1=0;idx1< im.getSize();idx1 +=oneRow){

          for(i=0;i<shiftIm;i++)

             _resObject.setShort(idx1+i,im.getShort(idx1+i));

        }

        //last col

        shiftIm = _halfCol1*im.getNumBands();

        for(idx1=oneRow-shiftIm;idx1<im.getSize();idx1 += oneRow){

          for(i=0;i<_halfCol1;i++)

             _resObject.setShort(idx1+i,im.getShort(idx1+i));

        }





      }// end of Short



     // compute euclidean between eroded vector and original vector

     // if you would like to compute edge map

     return true;

   }



   public boolean MorphoEdge(ImageObject im) throws ImageException{



     boolean ret = true;

     if(_edgeType.equalsIgnoreCase("DILATE"))

       ret = Dilate(im);

     else

       ret = Erode(im);

     if(!ret){

       return false;

    }



     if(_edgeMap == null){

      // allocate memory

       _edgeMap = ImageObject.createImage(im.getNumRows(), im.getNumCols(), 1, ImageObject.TYPE_FLOAT);

       _edgeMap.setSize(_edgeMap.getNumCols(),_edgeMap.getNumRows(),_edgeMap.getNumBands());
       
     }

    /*

      int _halfRow,_halfCol;

      _halfRow = (_morphElem.numrows-1) >> 1;

      if( (_halfRow << 1) != (_morphElem.numrows-1))

         _halfRow ++;



      _halfCol = (_morphElem.numcols-1) >> 1;

      if( (_halfCol << 1) != (_morphElem.numcols-1))

         _halfCol ++;

      int _halfRow1 = _morphElem.numrows - _halfRow;

      int _halfCol1 = _morphElem.numcols - _halfCol;

     */



      int shiftIm = (_halfRow*im.getNumCols() + _halfCol)*im.getNumBands();

      int shiftImRow = _morphElem.getNumCols()*im.getNumBands();

      boolean signal;

      int i,j;

      int idx1,idxIm=0;

      int oneRow = im.getNumCols() * im.getNumBands();

      float minEdge = _lim.MAX_FLOAT;



      if(im.getType()==ImageObject.TYPE_BYTE){



        idxIm = shiftIm;

        idx1 = _halfRow*_edgeMap.getNumCols() + _halfCol;

        for(i=_halfRow;i<im.getNumRows()-_halfRow1;i++){

          for(j=_halfCol;j<im.getNumCols()-_halfCol1;j++){

              _edgeMap.setFloat(idx1,(float)_myGeom.euclidDist(im.getNumBands(), (byte[])_resObject.getData(), idxIm, (byte[])im.getData(), idxIm));

              if(_edgeMap.getFloat(idx1) < minEdge){

                minEdge = _edgeMap.getFloat(idx1);

              }

              idxIm += im.getNumBands();

              idx1++;

          }

          idxIm += shiftImRow;

          idx1 += _morphElem.getNumCols();

        }



      }// end of BYTE

      if(im.getType()==ImageObject.TYPE_SHORT){

        idxIm = shiftIm;

        idx1 = _halfRow*_edgeMap.getNumCols() + _halfCol;

        for(i=_halfRow;i<im.getNumRows()-_halfRow1;i++){

          for(j=_halfCol;j<im.getNumCols()-_halfCol1;j++){

              _edgeMap.setFloat(idx1,(float)_myGeom.euclidDist(im.getNumBands(), (short[])_resObject.getData(), idxIm, (short[])im.getData(), idxIm));

              if(_edgeMap.getFloat(idx1) < minEdge){

                 minEdge = _edgeMap.getFloat(idx1);

              }



              idxIm += im.getNumBands();

              idx1++;

          }

          idxIm += shiftImRow;

          idx1 += _morphElem.getNumCols();

        }



      }// end of SHORT





      // take care of the borders

      // top rim

      idx1 = _halfRow*_edgeMap.getNumCols() + _halfCol;

      for(i=0;i<idx1;i++)

        _edgeMap.setFloat(i,minEdge);//0F;



      // bottom rim

      shiftIm = _halfRow1*_edgeMap.getNumCols()+_halfCol1;

      for(i=(int)_edgeMap.getSize()-shiftIm;i<_edgeMap.getSize();i++)

        _edgeMap.setFloat(i,minEdge);//0F;



      //first col

      shiftIm = _halfCol;

      for(idx1=0;idx1< _edgeMap.getSize();idx1 +=oneRow){

        for(i=0;i<shiftIm;i++)

          _edgeMap.setFloat(idx1+i,minEdge);//0F;

      }

      //last col

      shiftIm = _halfCol1;

      for(idx1=oneRow-shiftIm;idx1<_edgeMap.getSize();idx1 += oneRow){

        for(i=0;i<_halfCol1;i++)

          _edgeMap.setFloat(idx1+i,minEdge);//0F;

      }



    return true;

   }



} // end of class Morpho









