package edu.illinois.ncsa.isda.imagetools.ext.hyperspectral;
/*
 * RankBand.java
 *
 */

import java.io.*;
import java.lang.Math;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.datatype.LimitValues;
import edu.illinois.ncsa.isda.imagetools.core.datatype.SubArea;
import edu.illinois.ncsa.isda.imagetools.ext.conversion.SpFilters;
import edu.illinois.ncsa.isda.imagetools.ext.statistics.Histogram;
import edu.illinois.ncsa.isda.imagetools.ext.statistics.ImStats;




/**
<B>
The class RankBand provides a tool for ordering image bands based
on five measures, such as, entropy, contrast, ratio, similarity of adjacent bands and
predictability of a band based on adjacent bands.
</B>
<BR>
<BR>
<B>Description:</B>
<BR>
<B>Method 1: </B> The band entropy measure is computed from each band separately using information entropy.

<BR>
<img src="../../../../../../images/entropyEq.jpg" width="137" height="37">
<BR>

H is entropy measure, p is the probability of a reflectance value in a hyperspectral band and
m is the number of distinct reflectance values.
The probabilities are estimated by computing a histogram of reflectance values.
Generally, if the entropy value H is high then the amount of information in the data is large.
Thus, the bands are ranked in the ascending order from the band with the highest entropy value
(large amount of information) to the band with the smallest entropy value (small amount of information).

<p>
<BR>
<B>Method 2: </B> The band contrast measure is based on the assumption that the input data are used
for classification (discrimination) purposes and therefore high contrast among classes is desirable.
This measure evaluates sharpness (pixel contrast) of each band separately. The larger the contrast measure,
the better the discrimination. The score for each band is normalized with respect to a theoretical model
of a sharp image, such as, a checkerboard image of black and white pixels.

<BR>
<img src="../../../../../../images/rankContrastEqB.jpg" width="302" height="46">
<BR>

f is the probability density function of all contrast values computed across one band and
lambda is the band characteristic.
The equation includes the contrast magnitude term and the term with the likelihood of contrast occurrence.
</p>

<p>
<BR>
<B>Method 3: </B> The similarity of adjacent bands is evaluated based on the first derivative
along the band index axis.

<BR>
<img src="../../../../../../images/rankFirstDerEq.jpg" width="137" height="41">
<BR>

I represents the multi-band value and lambda is the band characteristic.
Thus, if D1 is equal to zero then one of the bands is redundant.
</p>

<p>
<BR>
<B>Method 4: </B> The second derivative along the band axis provides a measure
based on linear interpolation (prediction) of a band with its adjacent bands.
<BR>
<img src="../../../../../../images/rankSecondDerEq.jpg" width="137" height="44">
<BR>
D2 represents the measure of linear deviation, I is a multi-band value and
lambda is the band characteristic.
Similarly to D1, this method explores the bandwidth variable in multi-band imagery
as a function of added information. In contrary to the D1 measure, this approach identifies bands that
 can be represented by a linear combination of the adjacent bands. Thus, if two adjacent bands can
 linearly interpolate any band, then the band is redundant.
</p>


<p>
<BR>
<B>Method 5: </B> In many practical cases, band ratios are effective in revealing information about inverse
relationship between spectral responses to the same phenomenon (e.g., living vegetation
using the normalized difference vegetation index), as well as discovering possible calibration
issues among bands. This method explores the band ratio quotients for ranking bands and identifies
bands that differ just by a scaling factor. The larger the deviation from the sample mean scaling
factor, the higher the RatioM value of the band. The mathematical description of this method
is shown below, where RatioM represents the measure, I is a hyperspectral value, x is a spatial location
and lambda is the band characteristic.

<BR>
<img src="../../../../../../images/rankRatioEqB.jpg" width="342" height="64">
<BR>
</p>

<p>
<BR>
<B>Method 6: </B> One of the standard measures of band similarity is also a normalized correlation.
The normalized correlation metric is a statistical measure that performs well if
a signal-to-noise ratio is large enough. This measure is also less sensitive to local mismatches since
it is based on a global statistical match.
<BR>
The correlation based band ordering computes the normalized correlation measure for all adjacent pairs of bands.
The bands are ordered from the smallest correlation value to the largest one since two bands contain redundant
information if they are highly correlated.
The mathematical description of the normalized correlation measure
is shown below, where CorrelationM represents the measure, I is a hyperspectral value, x is a spatial location
and lambda is the band characteristic. E denotes an expected value and sigma is a standard deviation.

<BR>
<img src="../../../../../../images/rankCorrelationEqB.jpg" width="408" height="57">
<BR>

</p>

<p>
<B>Run:</B>
Each method can be selected from a drop-down menu denoted as "RankOrderType". By clicking the "Order" and
"ShowOrder" buttons, the results will appear sorted from the most "important" band to the least "important"
band with their associated scores scaled between [0,100]. The scores are scaled in order to compare results
generated by several methods. In addition to showing results in the text area (part of the dialog),
the results are plotted in a new window frame.
<BR>

<BR>

<img src="../../../../../../images/rankBandDialog.jpg" width="398" height="397">

<img src="../../../../../../images/rankBandPlot.jpg" width="605" height="445">

<BR>

If all methods should be run then the execution is launched by clicking "OrderAll" button. The results are
displayed in the text area (part of the dialog) and in a Plot Window frame (see below)
after initiating "ShowOrder" button.
Saving the results is enabled via "Save" button. The format of the output file is a space delimited text file
that can be easily loaded into Microsoft Excel spread sheet.
<BR>

<BR>

<img src="../../../../../../images/rankBandPlot_01B.jpg" width="605" height="445">

<BR>
</p>


Release notes:
</B>
<BR>

@author  Peter Bajcsy
@version 1.0

*/



////////////////////////////////////////////////////////////////////
// this class orders multiple bands based on  their information content
////////////////////////////////////////////////////////////////////
public class RankBand {

    private boolean _debugRankBand;
    LimitValues _lim = new LimitValues();
    Histogram  _myHist = new Histogram();

    private int [] _orderBands;
    private double [] _scoreBands;

    private double [] _minVal = null;
    private double [] _maxVal = null;

    /**
     Class constructor.
    */
    public void RankBand(){
    	_debugRankBand = true;
        ResetMemory();
      }

    // Getters
    public int [] GetOrderBands(){
	   return _orderBands;
    }
    public int GetOrderBands(int index){
      if(_orderBands != null && index >=0 && index < _orderBands.length)
	return _orderBands[index];
      else
        return -1;
    }


    public double [] GetScoreBands(){
	return _scoreBands;
    }
    public double GetScoreBands(int index){
      if(_scoreBands != null && index >=0 && index < _scoreBands.length)
	return _scoreBands[index];
      else
        return -1.0;
    }


    //Setters
    public void SetDebugFlag(boolean flag){
	_debugRankBand = flag;
    }


    // initialize the output ordered array
    private void InitMemory(ImageObject im){
       _orderBands = new int [im.getNumBands()]; 
       _scoreBands = new double [im.getNumBands()];
    }
    // this method prepares the histogram bins depending on the data type
    private void PrepareHistBins(ImageObject im){

     if(im.getTypeString().equalsIgnoreCase("BYTE")){
        _myHist.SetIs256Bins(true);
      }
      if(im.getTypeString().equalsIgnoreCase("SHORT")){
        _myHist.SetMinDataVal(0.0);
        _myHist.SetMaxDataVal((double)(_lim.MAXPOS_SHORT-1) );//65535.0;
        if(_myHist.GetNumBins() != _lim.MAXPOS_SHORT){
          // memory allocation only if necessary
        	_myHist.SetNumBins(_lim.MAXPOS_SHORT);// default number of bins is 256
        }else{
        	_myHist.SetWideBins(1.0);
        }
        _myHist.SetIs256Bins(false);        
      }
      if(im.getTypeString().equalsIgnoreCase("INT") || im.getTypeString().equalsIgnoreCase("LONG") || im.getTypeString().equalsIgnoreCase("FLOAT") || im.getTypeString().equalsIgnoreCase("DOUBLE")){
        _myHist.SetIs256Bins(false);

        double absMin, absMax;
        absMin = Double.MAX_VALUE;//_lim.MAX_DOUBLE;
        absMax = Double.MIN_VALUE;//_lim.MIN_DOUBLE;
        im.computeMinMax();
        absMin = im.getMin();
        absMax = im.getMax();
        
        int band;

        _myHist.SetMinDataVal(absMin);
        _myHist.SetMaxDataVal(absMax);
        // test
        //System.out.println("TEST: absMin = "+ absMin + ", absMax = " + absMax);

        // we will use the same number of bins as for the short data type because the hyperspectral short type data
        // is short and the double type data are collected on the ground but should be matched to air data.
        if( (int) (absMax - absMin + 0.5) > _lim.MAXPOS_SHORT){
          _myHist.SetNumBins( (int)(_lim.MAXPOS_SHORT) );
        }else{
          if(absMax - absMin<256)
	     _myHist.SetNumBins(256);
          else
            _myHist.SetNumBins( (int) (absMax - absMin + 0.5)  );
        }

      }

    }
    // delete memory
    private void ResetMemory(){
        _orderBands = null;
        _scoreBands = null;
        _minVal = _maxVal = null;
    }

    //////////////////////////////////////////////////////////////
    // doers
    // This method ranks all bands based on entropy measure
    public boolean RankBasedEntropy(ImageObject im){
      //sanity check
      if(im == null ){
        System.out.println("Error: no input data");
        return false;
      }
      //if(im.getTypeString().equalsIgnoreCase("BYTE")==false && im.getTypeString().equalsIgnoreCase("SHORT")==false
      //&& im.getTypeString().equalsIgnoreCase("DOUBLE")==false){
      //if(im.getTypeString().equalsIgnoreCase("LONG") ){
      //  System.out.println("Error: other than BYTE or SHORT or INT or FLOAT or DOUBLE image is not supported");
      //  return false;
      //}
      // initialize internal memory for the result
      InitMemory(im);
      // init hist bins depending on the data type
      PrepareHistBins(im);

      // compute entropy measure for each band
      int band;
      double maxlog = -Math.log(1.0/(double)_myHist.GetNumBins() );
      //double rangelog = (Math.log(1.0) - minlog)/100.0; // maxlog - minlog
      double rangelog = maxlog/100.0; // maxlog - (minlog=0)

      for(band = 0; band < im.getNumBands(); band ++){
        //init orderBands
        _orderBands[band] = band;

        /***********temp code**************/
        //load in subsampled version of file
        /*SubArea area = new SubArea(0,0,100,100,false);
        ImInout myInout = new ImInout();
        try {
          myInout.ImLoad("/home/talumbau/geodata2/entropytest/cd1area0-609-200-159ss3point5.iip", area);
        } catch (Exception e){
          System.err.println("didn't work.");
        }
        ImageObject newIm = myInout.GetImageObject();

        //for short image only
        /*_myHist.SetIs256Bins(false);
        _myHist.SetMinDataVal(0.0);
        _myHist.SetMaxDataVal(255.0);
        _myHist.SetNumBins(256);*/

        /*if(_myHist.Hist(newIm,band) == false){
          System.out.println("Error: Could not compute hist");
          return false;
        }
        _myHist.Entropy();
        _scoreBands[band] = 100.0 - (maxlog - _myHist.GetEntropy() )/rangelog;*/

        /***********end temp code *******************/

        /**********original code**********************/
        try{
        _myHist.Hist(im,band);
        }catch(Exception e){
        	System.err.println("exception in histogram calculation e="+e);
        	return false;
        }
        _myHist.Entropy();
        _scoreBands[band] = 100.0 - (maxlog - _myHist.GetEntropy() )/rangelog;
        /************end original code***************/
        //test
       // System.out.println("Test: Entropy["+band+"]="+ _myHist.GetEntropy() );
        //System.out.println("Test: Normalized Entropy["+band+"]="+ _scoreBands[band]);
      }

      // sort based the entropy measures from max entropy to min entropy
      SortScore();
      return true;
   }

   ///////////////////////////////
   // This method ranks all bands based on image sharpness measure
    public boolean RankBasedSharpness(ImageObject im) throws Exception {
      //sanity check
      if(im == null ){
        System.out.println("Error: no input data");
        return false;
      }
      if(im.getTypeString().equalsIgnoreCase("BYTE")==false && im.getTypeString().equalsIgnoreCase("SHORT")==false
      && im.getTypeString().equalsIgnoreCase("DOUBLE")==false){
        System.out.println("Error: other than BYTE or SHORT or DOUBLE image is not supported");
        return false;
      }
      /*
      if( im.numrows < 6 || im.numcols < 6){
        System.out.println("Error: image does not have minimum spatial size (6x6) required for doing spatial computation");
        return false;
      }
      */
      // initialize internal memory for the result
      InitMemory(im);

      // compute sharpness measure for each band

      int i,band;
      for(band = 0; band < im.getNumBands(); band ++){
        //init orderBands
        _orderBands[band] = band;
      }

      SpFilters _mySpFilters = new SpFilters();
      int r,c,h,w;

      r = 5;
      if(im.getNumRows() < r+1)
        r = im.getNumRows();
      c = 5;
      if(im.getNumCols() < c+1)
        c = im.getNumCols();

      ImageObject kernel = null;
      try{
      kernel = ImageObject.createImage(r,c,1, "INT");
      }catch(Exception e){
    	  System.err.println("ERROR: could not create a kernel image; e="+e);
      }
      for(int index=0;index< kernel.getSize();index++){
    	  kernel.set(index, 1);
      }
      _mySpFilters.setKernel(kernel);
      _mySpFilters.setImage(im);
      _mySpFilters.filter(_mySpFilters.HIGH_PASS); // HIGH_PASS
      
      ImageObject hiim = _mySpFilters.getResult();//HighPassOut(im);
      if(hiim == null){
        System.out.println("Error: internal hipass error");
        return false;
      }
      // init hist bins depending on the data type
      PrepareHistBins(hiim);

      r = c = 0;
      h = hiim.getNumRows();
      w = hiim.getNumCols();
      int KernelRow = kernel.getNumRows();// _mySpFilters.GetKernelRowSize();
      int KernelCol = kernel.getNumCols();//_mySpFilters.GetKernelColSize();

      // extension to tiling is possible
      int TileX, TileY;
      TileX = TileY = 1;
      int Sensitivity = 1;

      int r1,c1,stepr,stepc;
      stepr = (h-2*KernelRow)/TileX;
      stepc = (w-2*KernelCol)/TileY;
      // special treatment for 1D signals
      if(stepr <=0 ){
        stepr = 1;
        KernelRow = 0;
      }
      if(stepc <=0 ){
        stepc = 1;
        KernelCol = 0;
      }
      SubArea area = new SubArea(0,0,stepc,stepr);//new SubArea(0,0,stepr,stepc,true,false);

      double max,tmp,prev;
      int storer,storec;
      storer = storec = -1;
      int dis_mean;
      double sum,sum1,help;

      int _Method = 0; // this will become global and will have a setter and getter later
      for(band = 0; band < im.getNumBands(); band ++){

        help = 0.0;
        switch(_Method){
          case 0:
            // This method is based on hist mean as a reference point
            // it has a good perormance in extreme cases
            max=0.0;
            for(r1=r+KernelRow;r1+stepr<=r+h-KernelRow;r1+=stepr){
             for(c1=c+KernelCol;c1+stepc<=c+w-KernelCol;c1+=stepc){
                area.y = r1;// row
                area.x = c1; // col
                /*
                // test
                System.out.println("Test:area");
                area.PrintSubArea();
                System.out.println("Test:absMin="+_myHist.GetMinDataVal()+", absMax="+_myHist.GetMaxDataVal());
                System.out.println("Test:BinWidth="+_myHist.GetWideBins());
                */
                _myHist.Hist(hiim,band,area);

/*
                // test
                for(i=0;i<_myHist.GetNumBins();i++){
                  if (_myHist._HistData[i] != 0)
                    System.out.print(" Hist["+i+"]="+_myHist._HistData[i]);
                }
                System.out.println("");
*/
                /*
                sum=0;
                sum1=0;
                for(i=0;i<256;i++){
                 sum+=_myHist._HistData[i];
                 sum1= sum1 + i * _myHist._HistData[i];
                }
                sum1= sum1/sum; // mean of the hist distribution
                //sum1=32.0;
                */
                _myHist.Mean();

                help=0;
                //dis_mean=(int)(sum1+0.5); // discrete mean
                dis_mean=(int)(_myHist.GetMean()+0.5); // discrete mean

                //test
                //System.out.println("Test: hist mean="+_myHist.GetMean()+", [0]="+ _myHist._HistData[0]  );
                //System.out.println("Test: dis_mean="+ dis_mean+", Sensitivity="+ Sensitivity +", GetNumBins="+ _myHist.GetNumBins() );

                
                for(i= dis_mean+Sensitivity;i<_myHist.GetNumBins();i++)
                  help+= _myHist.GetHistData()[i]*(i-dis_mean);

                for(i=0;i< dis_mean-Sensitivity;i++)
                  help+= _myHist.GetHistData()[i]*(dis_mean-i);

                //test
                //System.out.println("Test: measure at band="+band+" row="+r1+"'col="+c1+" is "+help );

                if(help>max){
                  storer=r1;
                  storec=c1;
                  max=help;
                }
             }
            }
            help=max;
           break;
          case 1:
            // This method is based on the theoretical reference point equals to 128
            max=0;
            for(r1=r+KernelRow;r1+stepr<=r+h-KernelRow;r1+=stepr){
             for(c1=c+KernelCol;c1+stepc<=c+w-KernelCol;c1+=stepc){
                area.y = r1;//row
                area.x = c1;//col
            
                _myHist.Hist(hiim,band,area);

                help=0;
                int refVal = (_myHist.GetNumBins()>>1);
                for(i=0;i<refVal-Sensitivity;i++)
                  help+=_myHist.GetHistData()[i]*(refVal-i);
                for(i=refVal+Sensitivity;i<_myHist.GetNumBins();i++)
                  help+=_myHist.GetHistData()[i]*(i-refVal);

                if(help>max){
                  storer=r1;
                  storec=c1;
                  max=help;
                }
             }
            }
            help=max;
            break;
          case 2:
           // this method is similar to method1 but two adjacent local results are
           // averaged to eliminate the problem of a block border intersecting features
            max=0;
            for(r1=r+KernelRow;r1+stepr<=r+h-KernelRow;r1+=stepr){
              prev=-1;
              for(c1=c+KernelCol;c1+stepc<=c+w-KernelCol;c1+=stepc){
                area.y = r1;//row
                area.x = c1;//col
                _myHist.Hist(hiim,band,area);

                help=0;
                int refVal = (_myHist.GetNumBins()>>1);
                for(i=0;i<refVal-Sensitivity;i++)
                  help+=_myHist.GetHistData()[i]*(refVal-i);
                for(i=refVal+Sensitivity;i<_myHist.GetNumBins();i++)
                  help+=_myHist.GetHistData()[i]*(i-refVal);

                if(prev!=-1){
                  tmp=(help+prev)*0.5;
                  prev=help;
                  if(tmp>max){
                   storer=r1;
                   storec=c1;
                   max=tmp;
                  }
                }else{
                  if(TileY!=1){
                    prev=help;
                    help=0;
                  }else{
                    if(help>max){
                      storer=r1;
                      storec=c1;
                      max=help;
                    }
                  }
                }

             }
            }
            help=max;
            break;
          default:
            System.out.println("Error: method is not supported");
            help = 0.0;
          break;
        }// end of case
        //System.out.println("Test: before assignment");
        _scoreBands[band] = help;
        //test
        // this should be tested when tiling is on
        //System.out.println("Test: band="+band+", score="+help+", locRow="+storer+", locCol="+storec);

      }//end for(band) loop


      //////////////////////////////////////////////////////
      // sort  from max  to min
      SortScore();
      //scale the value to 0 -100
      // the assumption is that a checkerboard pattern would generate the highest possible
      // score
      double scale=1.0;
      tmp = 1.0;
      if( (im.getNumRows()-KernelRow) > 0  ){
        tmp *= (im.getNumRows()-KernelRow);
      }
      if( (im.getNumCols()-KernelCol) > 0 ){
        tmp *= (im.getNumCols()-KernelCol);
      }
      scale = 100.0/( (_myHist.GetNumBins() >> 1 ) * tmp);
      //test
      System.out.println("Test: scale for sharpness="+scale);

      for(band = 0; band < im.getNumBands(); band ++){
        _scoreBands[ band ] *= scale;
     }

      return true;
   }

  ///////////////////////////////////////////////////////////////////////////
  // This method ranks all bands based on piecewise linear approximation
  // 2nd deriv = 0
    public boolean RankBased2ndDeriv(ImageObject im){
      //sanity check
      if(im == null ){
        System.out.println("Error: no input data");
        return false;
      }
      if( im.getTypeString().equalsIgnoreCase("BYTE")==false && im.getTypeString().equalsIgnoreCase("SHORT")==false
      && im.getTypeString().equalsIgnoreCase("DOUBLE")== false){
        System.out.println("Error: other than BYTE or SHORT or DOUBLE image is not supported");
        return false;
      }
      // initialize internal memory for the result
      InitMemory(im);

      // given three bands, compute distance from the middle band value
      // to the line defined by the values from adjacent bands
      // sum together all distances and average them = measure
      int idx,band;
      double val0,val1,val2,estError;

      //init
      for(band = 0; band < im.getNumBands(); band ++){
        //init orderBands
        _orderBands[band] = band;
        _scoreBands[ band ] = 0.0;
      }
/*
        // compute measure for all bands
        for(band = 0; band < im.getNumBands(); band ++){
          for( idx=band; idx < im.getSize(); idx += im.getNumBands()){
           // three adjacent bands
            if(band==0 || band == im.getNumBands()-1){
              if(band==0){
                // the first band
                val0 = im.getDouble(idx);
                if(val0 < 0)
                  val0 += _lim.MAXPOS_BYTE;
                val1 = im.getDouble(idx+1);
                if(val1 < 0)
                  val1 += _lim.MAXPOS_BYTE;
                val2 = im.getDouble(idx+2);
                if(val2 < 0)
                  val2 += _lim.MAXPOS_BYTE;
                estError = val1 - (val2 + val0)*0.5;//val0 - (2.0*val1 - val2);
              }else{
                // the last band
                val0 = im.getDouble(idx-2);
                if(val0 < 0)
                  val0 += _lim.MAXPOS_BYTE;
                val1 = im.getDouble(idx-1);
                if(val1 < 0)
                  val1 += _lim.MAXPOS_BYTE;
                val2 = im.getDouble(idx);
                if(val2 < 0)
                  val2 += _lim.MAXPOS_BYTE;
                estError = val1 - (val2 + val0)*0.5;//val2 - (2.0*val1 - val0);
              }
            }else{
              // any band in the middle
              val0 = im.getDouble(idx-1);
              if(val0 < 0)
                val0 += _lim.MAXPOS_BYTE;
              val1 = im.getDouble(idx);
              if(val1 < 0)
                val1 += _lim.MAXPOS_BYTE;
              val2 = im.getDouble(idx+1);
              if(val2 < 0)
                val2 += _lim.MAXPOS_BYTE;
              estError = val1 - (val2 + val0)*0.5;
            }
            // take absolute values
            if(estError<0.0)
              estError = -estError;

            _scoreBands[ band ] += estError;
          }
        }
*/
        if(_minVal == null || _minVal.length != im.getNumBands())
          _minVal = new double [im.getNumBands()];

        if(_maxVal == null || _maxVal.length != im.getNumBands())
          _maxVal = new double[im.getNumBands()];

        for(band = 0; band < im.getNumBands(); band ++){
          _minVal[band] = _lim.MAX_DOUBLE;
          _maxVal[band] = _lim.MIN_DOUBLE;
        }
        // compute measure for all bands
        for(band = 0; band < im.getNumBands(); band ++){
          for( idx=band; idx < im.getSize(); idx += im.getNumBands()){
           // three adjacent bands
            if(band==0 || band == im.getNumBands()-1){
              if(band==0){
                // the first band
                val0 = im.getDouble(idx);
                val1 = im.getDouble(idx+1);
                val2 = im.getDouble(idx+2);

                estError = val1 - (val2 + val0)*0.5;;//val0 - (2.0*val1 - val2);
              }else{
                // the last band
                val0 = im.getDouble(idx-2);
                val1 = im.getDouble(idx-1);
                val2 = im.getDouble(idx);

                estError = val1 - (val2 + val0)*0.5;; //val2 - (2.0*val1 - val0);
              }
            }else{
              // any band in the middle
              val0 = im.getDouble(idx-1);
              val1 = im.getDouble(idx);
              val2 = im.getDouble(idx+1);

              estError = val1 - (val2 + val0)*0.5;
            }
            // take absolute values
            if(estError<0.0)
              estError = -estError;

            if(_minVal[band] > estError)
              _minVal[band] = estError;
            if(_maxVal[band] < estError)
              _maxVal[band] = estError;

            _scoreBands[ band ] += estError;
          }
        }
 

      //scale the value to 0 -100
      ScaleScore(im);
      // sort based the measures from max to min values
      // the larger the value of error the more important the band !!
      SortScore();

      return true;
   }

  ///////////////////////////////////////////////////////////////////////////
  // This method ranks all bands based on band difference approximation
  // 1st deriv = 0
    public boolean RankBased1stDeriv(ImageObject im){
      //sanity check
      if(im == null ){
        System.out.println("Error: no input data");
        return false;
      }
      if(im.getTypeString().equalsIgnoreCase("BYTE")==false && im.getTypeString().equalsIgnoreCase("SHORT")==false
        && im.getTypeString().equalsIgnoreCase("DOUBLE")==false){
        System.out.println("Error: other than BYTE or SHORT or DOUBLE image is not supported");
        return false;
      }
      // initialize internal memory for the result
      InitMemory(im);

      // given two bands, compute intensity difference
      int idx,band;
      double val0,val1,val2,estError;

      //init
      for(band = 0; band < im.getNumBands(); band ++){
        //init orderBands
        _orderBands[band] = band;
        _scoreBands[ band ] = 0.0;
      }
/*
      if(im.getTypeString().equalsIgnoreCase("BYTE")){
        // compute measure for every pair of adjacent bands
        for(band = 0; band < im.getNumBands()-1; band ++){
          for( idx=band; idx < im.size; idx += im.getNumBands()){
            if( (im.image[idx+1] >= 0 &&  im.image[idx] >= 0 ) || (im.image[idx+1] < 0 &&  im.image[idx] < 0 ) ){
              estError = im.image[idx+1] - im.image[idx];
            }else{
              if(im.image[idx+1] < 0 )
                estError = im.image[idx+1] + _lim.MAXPOS_BYTE - im.image[idx];
              else
                estError = im.image[idx+1]  - im.image[idx] - _lim.MAXPOS_BYTE;
            }
            // take absolute values
            if(estError<0.0)
              estError = -estError;

            _scoreBands[ band ] += estError;
          }
        }
        _scoreBands[ im.getNumBands()-1 ] =  _scoreBands[ im.getNumBands() - 2 ];

      }// end of BYTE data
      if(im.getTypeString().equalsIgnoreCase("SHORT")){
        // compute measure for every pair of adjacent bands
        for(band = 0; band < im.getNumBands()-1; band ++){
          for( idx=band; idx < im.size; idx += im.getNumBands()){
            val0 = im.imageShort[idx+1];
            val1 = im.imageShort[idx];
            if(val0 < 0)
              val0 += _lim.MAXPOS_SHORT;
            if(val1 < 0)
              val1 += _lim.MAXPOS_SHORT;

            estError = val0 - val1;//im.imageShort[idx+1] - im.imageShort[idx];
            // take absolute values
            if(estError<0.0)
              estError = -estError;

            _scoreBands[ band ] += estError;
          }
        }
        _scoreBands[ im.getNumBands()-1 ] =  _scoreBands[ im.getNumBands() - 2 ];

      }// end of SHORT data
*///      if(im.getTypeString().equalsIgnoreCase("DOUBLE")){
        if(_minVal == null || _minVal.length != im.getNumBands())
          _minVal = new double [im.getNumBands()];

        if(_maxVal == null || _maxVal.length != im.getNumBands())
          _maxVal = new double [im.getNumBands()];

        for(band = 0; band < im.getNumBands(); band ++){
          _minVal[band] = _lim.MAX_DOUBLE;
          _maxVal[band] = _lim.MIN_DOUBLE;
        }

        // compute measure for every pair of adjacent bands
        for(band = 0; band < im.getNumBands()-1; band ++){
          for( idx=band; idx < im.getSize(); idx += im.getNumBands()){
            val0 = im.getDouble(idx+1);
            val1 = im.getDouble(idx);

            estError = val0 - val1;
            // take absolute values
            if(estError<0.0)
              estError = -estError;

            if(_minVal[band] > estError){
              _minVal[band] = estError;
              //System.out.println("Test:estError->minVal"+estError+", score["+band+"]="+_scoreBands[ band ]);
            }
            if(_maxVal[band] < estError){
              _maxVal[band] = estError;
              //System.out.println("Test:estError->maxVal"+estError+"score["+band+"]="+_scoreBands[ band ]);
            }

            _scoreBands[ band ] += estError;
          }
        }
        _scoreBands[ im.getNumBands()-1 ] =  _scoreBands[ im.getNumBands() - 2 ];
        _minVal[ im.getNumBands()-1 ] =  _minVal[ im.getNumBands() - 2 ];
        _maxVal[ im.getNumBands()-1 ] =  _maxVal[ im.getNumBands() - 2 ];

  //    }// end of DOUBLE data

     //test
     System.out.println("Test: before picking max of two adjacent");

     // for the middle bands , take the larger measure from two adjacent
     double prev,current;
/*
     prev = _scoreBands[0];
     for(band = 1; band < im.getNumBands()-1; band ++){
       current = _scoreBands[ band ];
       if(_scoreBands[ band ] < prev){
         _scoreBands[ band ] = prev;
       }
       prev = current ;
    }
*/
      //test
      //System.out.println("Test: before scaling");
      ScaleScore(im);

     //test
     //System.out.println("Test: before ordering");
    // sort based the measures from max to min values
      // the larger the value of error the more important the band !!
      SortScore();
      return true;
   }

  ///////////////////////////////////////////////////////////////////////////
  // This method ranks all bands based on spectral band ratio
  // if the ratio band(i)/band(i+1) is equal to the average ratio then the measure  = 0
    public boolean RankBasedRatio(ImageObject im){
      //sanity check
      if(im == null ){
        System.out.println("Error: no input data");
        return false;
      }
      if(im.getTypeString().equalsIgnoreCase("BYTE")==false && im.getTypeString().equalsIgnoreCase("SHORT")==false
        && im.getTypeString().equalsIgnoreCase("DOUBLE")==false){
        System.out.println("Error: other than BYTE or SHORT or DOUBLE image is not supported");
        return false;
      }
      // initialize internal memory for the result
      InitMemory(im);

      // given two bands, compute intensity difference
      int idx,band;
      double estError;

      //init
      for(band = 0; band < im.getNumBands(); band ++){
        //init orderBands
        _orderBands[band] = band;
        _scoreBands[ band ] = 0.0;
      }

      double [] mean = new double[im.getNumBands()];
      ImageObject ratio = null;
      try{
    	  ratio = ImageObject.createImage(im.getNumRows(),im.getNumCols(),im.getNumBands(),"DOUBLE");
      }catch(Exception e){
    	  System.err.println("Error: could not create an image; e="+e);
      }
      // init mean
      for(band = 0; band < im.getNumBands(); band ++){
          mean[band] = 0.0;
      }

      double tempVal;
      double val1, val2;
      int count = im.getNumRows() * im.getNumCols();
      int testCount = 0;
      // unsigned byte
/*      if(im.getTypeString().equalsIgnoreCase("BYTE")){
        // compute measure for every pair of adjacent bands

        // compute mean
        for(band = 0; band < im.getNumBands()-1; band ++){
          testCount = 0;
          for( idx=band; idx < im.getSize(); idx += im.getNumBands()){
            if( im.image[idx+1] != 0){
               val1 = im.getDouble(idx+1);
               if(val1 < 0 )
                 val1 += _lim.MAXPOS_BYTE;
               val2 = im.image[idx];
               if(val2 < 0 )
                 val2 += _lim.MAXPOS_BYTE;

               tempVal = val2/val1;
               //test
               //if(tempVal != 1.0)
               //  System.out.println("Test tempVal="+tempVal+",band="+band+",val1="+val1+", val2="+val2);

               ratio.imageDouble[idx] = tempVal;
               mean.imageDouble[band] += tempVal;
            }else{
              if( im.image[idx] == 0){
                ratio.imageDouble[idx] = 1.0;
                mean.imageDouble[band] += 1.0;
              }else{
                ratio.imageDouble[idx] = _lim.MAXPOS_BYTE;
                mean.imageDouble[band] += _lim.MAXPOS_BYTE;
               //test
               //System.out.println("Test divide zero: val1="+im.image[idx]+", val2="+im.image[idx+1]);

              }
            }
            testCount++;
          }
          //System.out.println("Test before divide: ="+mean.imageDouble[band]+", count="+count+", testCount="+testCount);
          mean.imageDouble[band] /= (double)count;
        }
      }// end of BYTE data

      // unsigned short !!!
      if(im.getTypeString().equalsIgnoreCase("SHORT")){
        // compute measure for every pair of adjacent bands

        // compute mean
        for(band = 0; band < im.getNumBands()-1; band ++){
          for( idx=band; idx < im.size; idx += im.getNumBands()){
            if( im.imageShort[idx+1] != 0){

               // treated as java unsigned short
               val1 = im.imageShort[idx+1];
               if(val1 < 0 )
                 val1 += _lim.MAXPOS_SHORT;
               val2 = im.imageShort[idx];
               if(val2 < 0 )
                 val2 += _lim.MAXPOS_SHORT;

                // treated as signed short
               //val1 = im.imageShort[idx+1];// - _lim.MIN_SHORT;
               //val2 = im.imageShort[idx];// - _lim.MIN_SHORT;

               tempVal = val2/val1;
               ratio.imageDouble[idx] = tempVal;
               mean.imageDouble[band] += tempVal;
            }else{
              if( im.imageShort[idx] == 0){
                ratio.imageDouble[idx] = 1.0;
                mean.imageDouble[band] += 1.0;
              }else{
                ratio.imageDouble[idx] = _lim.MAXPOS_SHORT;
                mean.imageDouble[band] += _lim.MAXPOS_SHORT;
              }
            }
          }
          mean.imageDouble[band] /= (double)count;
        }

      }// end of SHORT data
*/     // if(im.getTypeString().equalsIgnoreCase("DOUBLE")){
        // compute measure for every pair of adjacent bands

        // compute mean
        for(band = 0; band < im.getNumBands()-1; band ++){
          for( idx=band; idx < im.getSize(); idx += im.getNumBands()){
            if( im.getDouble(idx+1) != 0){
               val1 = im.getDouble(idx+1);
               val2 = im.getDouble(idx);

               tempVal = val2/val1;
               ratio.set(idx,tempVal);
               mean[band] += tempVal;
            }else{
              if( im.getDouble(idx) == 0){
                ratio.set(idx, 1.0);
                mean[band] += 1.0;
              }else{
                // it is not clear what to assign here ?
                ratio.set(idx, 0.0);//_lim.MAX_DOUBLE;
                //mean.imageDouble[band] += _lim.MAX_DOUBLE;
              }
            }
          }
          mean[band] /= (double)count;
        }

      //}// end of DOUBLE data

      // final assignment
      if(_minVal == null || _minVal.length != im.getNumBands())
        _minVal = new double[im.getNumBands()];

      if(_maxVal == null || _maxVal.length != im.getNumBands())
        _maxVal = new double[im.getNumBands()];

      for(band = 0; band < im.getNumBands(); band ++){
        _minVal[band] = _lim.MAX_DOUBLE;
        _maxVal[band] = _lim.MIN_DOUBLE;
      }
      for(band = 0; band < im.getNumBands()-1; band ++){
        for( idx=band; idx < im.getSize(); idx += im.getNumBands()){
           estError =  mean[band] - ratio.getDouble(idx) ;

           // take absolute values
           if(estError<0.0)
            estError = -estError;

            if(_minVal[band] > estError)
              _minVal[band] = estError;
            if(_maxVal[band] < estError)
              _maxVal[band] = estError;

          _scoreBands[ band ] += estError;
        }
      }
      _scoreBands[ im.getNumBands()-1 ] =  _scoreBands[ im.getNumBands() - 2 ];
      _minVal[ im.getNumBands()-1 ] =  _minVal[ im.getNumBands() - 2 ];
      _maxVal[ im.getNumBands()-1 ] =  _maxVal[ im.getNumBands() - 2 ];

      ratio = null;
      mean = null;

      //test
      //System.out.println("Test: before scaling");
      ScaleScore(im);

     //test
     //System.out.println("Test: before ordering");
      SortScore();
      return true;
   }

  ///////////////////////////////////////////////////////////////////////////
  // This method ranks all bands based on normalized correlation of two adjacent spectral bands
   // original version, computes pair-wise adjacent correlation and ranks based on the values
   /*
    public boolean RankBasedCorrelation(ImageObject im){
      //sanity check
      if(im == null ){
        System.out.println("Error: no input data");
        return false;
      }
      if(im.getTypeString().equalsIgnoreCase("BYTE")==false && im.getTypeString().equalsIgnoreCase("SHORT")==false
      && im.getTypeString().equalsIgnoreCase("DOUBLE")==false ){
        System.out.println("Error: other than BYTE or SHORT or DOUBLE image is not supported");
        return false;
      }
      // initialize internal memory for the result
      InitMemory(im);

      // given two bands, compute intensity difference
      int idx,band;
      double estError;

      //init
      for(band = 0; band < im.getNumBands(); band ++){
        //init orderBands
        _orderBands[band] = band;
        _scoreBands[ band ] = 0.0;
      }

      /////////////////////////////////////////////////
      // compute mean and stdev of each band
      ImStats myImStats = new ImStats();
      if ( !myImStats.MeanStdevVal(im) ){
        System.out.println("Error: could not compute mean and stdev");
        return false;
      }
      ImageObject mean = null;
      ImageObject stdev = null;

      mean = myImStats.GetMeanVal();
      stdev = myImStats.GetStdevVal();
      if( mean == null || stdev == null){
        System.out.println("Error:  mean or/and stdev are null");
        return false;
      }

      double crossMean, val1;
      // unsigned byte
      // compute measure for every pair of adjacent bands
      // the measure is always positive although the normalized correlation
      // detects positive and negative relationship
      for(band = 0; band < im.getNumBands()-1; band ++){
         crossMean = myImStats.CrossMeanVal(im,band,band+1);
         if( stdev.imageDouble[band] > 0 && stdev.imageDouble[band+1] > 0 ){
             val1 = Math.abs( crossMean - mean.imageDouble[band]*mean.imageDouble[band+1] );
             val1 /= (stdev.imageDouble[band]*stdev.imageDouble[band+1]);
            _scoreBands[ band ] = 100.0 * (1.0 - val1);
            if(_scoreBands[ band ] < _lim.EPSILON)
             _scoreBands[ band ] = 0.0;
          }else{
            _scoreBands[ band ] = 0.0;
          }
      }
      _scoreBands[ im.getNumBands()-1 ] =  _scoreBands[ im.getNumBands() - 2 ];

     //test
     //System.out.println("Test: before ordering");
      SortScore();
      return true;
   }
*/
/*
    ////////////////////
   // this implementation picks the best, then re-computes correlation with the remaining
   // bands and picks the best, and so on.
   public boolean RankBasedCorrelation(ImageObject im){
      //sanity check
      if(im == null ){
        System.out.println("Error: no input data");
        return false;
      }
      if(im.getTypeString().equalsIgnoreCase("BYTE")==false && im.getTypeString().equalsIgnoreCase("SHORT")==false
      && im.getTypeString().equalsIgnoreCase("DOUBLE")==false ){
        System.out.println("Error: other than BYTE or SHORT or DOUBLE image is not supported");
        return false;
      }
      // initialize internal memory for the result
      InitMemory(im);

      // given two bands, compute intensity difference
      int idx,band;
      double estError;

      //init
      for(band = 0; band < im.getNumBands(); band ++){
        //init orderBands
        _orderBands[band] = band;
        _scoreBands[ band ] = 0.0;
      }

      /////////////////////////////////////////////////
      // compute mean and stdev of each band
      ImStats myImStats = new ImStats();
      if ( !myImStats.MeanStdevVal(im) ){
        System.out.println("Error: could not compute mean and stdev");
        return false;
      }
      ImageObject mean = null;
      ImageObject stdev = null;

      mean = myImStats.GetMeanVal();
      stdev = myImStats.GetStdevVal();
      if( mean == null || stdev == null){
        System.out.println("Error:  mean or/and stdev are null");
        return false;
      }

      double crossMean, val1;
      double maxScore = _lim.MIN_DOUBLE;
      int maxScoreBand = -1;
      // unsigned byte
      // compute measure for every pair of adjacent bands
      // the measure is always positive although the normalized correlation
      // detects positive and negative relationship
      for(band = 0; band < im.getNumBands()-1; band ++){
         crossMean = myImStats.CrossMeanVal(im,band,band+1);
         if( stdev.imageDouble[band] > 0 && stdev.imageDouble[band+1] > 0 ){
             val1 = Math.abs( crossMean - mean.imageDouble[band]*mean.imageDouble[band+1] );
             val1 /= (stdev.imageDouble[band]*stdev.imageDouble[band+1]);
            _scoreBands[ band ] = 100.0 * (1.0 - val1);
            if(_scoreBands[ band ] < _lim.EPSILON)
             _scoreBands[ band ] = 0.0;
          }else{
            _scoreBands[ band ] = 0.0;
          }
          if(_scoreBands[band] > maxScore){
            maxScore = _scoreBands[band];
            maxScoreBand = band;
          }
      }
      _scoreBands[ im.getNumBands()-1 ] =  _scoreBands[ im.getNumBands() - 2 ];

     //test
     //System.out.println("Test: before ordering");
      //SortScore();

     /////////////////////////////
      // pick best and re-compute correlation again
      ImageObject tempScoreBands = new ImageObject(1,1,im.getNumBands(), "DOUBLE");
      ImageObject tempOrderBands = new ImageObject(1,1,im.getNumBands(), "INT");


      int i;
      boolean signal = true;
      // setup the top ranked band
      int numRanked = 1;
      //int numBands = im.getNumBands() -1;

      // the last value is the maximum
      for(band=0, i=0;band<tempScoreBands.sampPerPixel;band++, i++){
        if(band == maxScoreBand){
          i--;
          tempScoreBands.imageDouble[tempScoreBands.sampPerPixel-1] = _scoreBands[band];
          tempOrderBands.imageInt[tempScoreBands.sampPerPixel-1] = _orderBands[band];
        }else{
          tempScoreBands.imageDouble[i]= _scoreBands[band];
          tempOrderBands.imageInt[i]= _orderBands[band];
        }
      }

      int bandLastRemoved = maxScoreBand;
      int b1, b2, b11, b21;
      // rank until at least 2 bands are left
      while(im.getNumBands() - numRanked > 2){

        //bandLastRemoved = tempOrderBands.imageInt[tempOrderBands.sampPerPixel -1 numRanked-1];
        // find smaller band
        signal = true;
        b1 = b2 = -1;
        b11 = b21 = -1;
        for(band=0; signal && band <tempScoreBands.sampPerPixel - numRanked ;band++){
          if( tempOrderBands.imageInt[band] < bandLastRemoved){
            b1 = tempOrderBands.imageInt[band];
            b11 = band;
          }
          if(signal && tempOrderBands.imageInt[band] > bandLastRemoved){
            signal = false;
            b2 = tempOrderBands.imageInt[band];
            b21 = band;
          }
        }

        //test
        if (numRanked < 3){
          System.out.println("TEST: bandlastremoved="+bandLastRemoved+",b1="+b1+",b2="+b2+",b11="+b11+",b21="+b21);
        }
        if(b1  != -1 && b2 != -1){
           // compute new normalized correlation value
           crossMean = myImStats.CrossMeanVal(im,b1,b2);
           if( stdev.imageDouble[b1] > 0 && stdev.imageDouble[b2] > 0 ){
               val1 = Math.abs( crossMean - mean.imageDouble[b1]*mean.imageDouble[b2] );
               val1 /= (stdev.imageDouble[b1]*stdev.imageDouble[b2]);
              tempScoreBands.imageDouble[ b11 ] = 100.0 * (1.0 - val1);
              if(tempScoreBands.imageDouble[ b11 ] < _lim.EPSILON)
               tempScoreBands.imageDouble[ b11 ] = 0.0;
            }else{
              tempScoreBands.imageDouble[ b11 ] = 0.0;
            }
            if(b21 == tempScoreBands.sampPerPixel -1 - numRanked){
              tempScoreBands.imageDouble[ tempScoreBands.sampPerPixel -1 - numRanked ] = tempScoreBands.imageDouble[ b11 ];
            }
        }else{
          // the end bands were removed and we do not have to re-compute anything
          ;
        }

        // find new maximum score
        maxScore = _lim.MIN_DOUBLE;
        maxScoreBand = -1;
        b11 = -1;
        for(band = 0; band < tempScoreBands.sampPerPixel - numRanked; band ++){
          if(tempScoreBands.imageDouble[band] > maxScore){
             maxScore = tempScoreBands.imageDouble[band];
             maxScoreBand = tempOrderBands.imageInt[band];
             b11 = band;
           }
        }

        //test
        if (numRanked < 3){
          System.out.println("TEST: maxScore="+maxScore+",maxScoreband="+maxScoreBand+",b11="+b11);
        }

        // reshuffle so that the last value moves to the end
        for(band=b11;band<tempScoreBands.sampPerPixel - 1 - numRanked;band++){
          tempScoreBands.imageDouble[band] = tempScoreBands.imageDouble[band+1];
          tempOrderBands.imageInt[band] = tempOrderBands.imageInt[band+1];
        }
        tempScoreBands.imageDouble[tempScoreBands.sampPerPixel - 1 - numRanked] = maxScore;
        tempOrderBands.imageInt[tempScoreBands.sampPerPixel - 1 - numRanked] = maxScoreBand;

        bandLastRemoved = maxScoreBand;
        numRanked ++;

        //test
        if (numRanked < 3){
          for(i=0;i<numRanked;i++){
            System.out.println("TEST: tempScoreBands.imageDouble["+i+"]="+tempScoreBands.imageDouble[tempScoreBands.sampPerPixel - 1 - i] );
            System.out.println("TEST: tempOrderBands.imageDouble["+i+"]="+tempOrderBands.imageInt[tempScoreBands.sampPerPixel - 1 - i] );
          }
        }

      }// end of while loop


      // copy final results into the internal ImageObjects
      for(band=0;band<tempScoreBands.sampPerPixel;band++){
        _scoreBands[band] = tempScoreBands.imageDouble[tempScoreBands.sampPerPixel-1 - band];
        _orderBands[band] = tempOrderBands.imageInt[tempScoreBands.sampPerPixel-1 - band];
      }


      return true;
    }
*/
   ////////////////////
  // this implementation picks the best, then re-computes correlation with respect
   // to the selected one and picks again the least correlated
  public boolean RankBasedCorrelation(ImageObject im){
     //sanity check
     if(im == null ){
       System.out.println("Error: no input data");
       return false;
     }
     if(im.getTypeString().equalsIgnoreCase("BYTE")==false && im.getTypeString().equalsIgnoreCase("SHORT")==false
     && im.getTypeString().equalsIgnoreCase("DOUBLE")==false ){
       System.out.println("Error: other than BYTE or SHORT or DOUBLE image is not supported");
       return false;
     }
     // initialize internal memory for the result
     InitMemory(im);

     // given two bands, compute intensity difference
     int idx,band;
     double estError;

     //init
     for(band = 0; band < im.getNumBands(); band ++){
       //init orderBands
       _orderBands[band] = band;
       _scoreBands[ band ] = 0.0;
     }

     /////////////////////////////////////////////////
     // compute mean and stdev of each band
     ImStats myImStats = new ImStats();
     if ( !myImStats.MeanStdevVal(im) ){
       System.out.println("Error: could not compute mean and stdev");
       return false;
     }
     double [] mean = null;
     double [] stdev = null;

     mean = myImStats.GetCopyMeanVal();
     stdev = myImStats.GetCopyStdevVal();
     if( mean == null || stdev == null){
       System.out.println("Error:  mean or/and stdev are null");
       return false;
     }

     double crossMean, val1;
     double maxScore = _lim.MIN_DOUBLE;
     int maxScoreBand = -1;
     // unsigned byte
     // compute measure for every pair of adjacent bands
     // the measure is always positive although the normalized correlation
     // detects positive and negative relationship
     for(band = 0; band < im.getNumBands()-1; band ++){
        crossMean = myImStats.CrossMeanVal(im,band,band+1);
        if( stdev[band] > 0 && stdev[band+1] > 0 ){
            val1 = Math.abs( crossMean - mean[band]*mean[band+1] );
            val1 /= (stdev[band]*stdev[band+1]);
           _scoreBands[ band ] = 100.0 * (1.0 - val1);
           if(_scoreBands[ band ] < _lim.EPSILON)
            _scoreBands[ band ] = 0.0;
         }else{
           _scoreBands[ band ] = 0.0;
         }
         if(_scoreBands[band] > maxScore){
           maxScore = _scoreBands[band];
           maxScoreBand = band;
         }
     }
     _scoreBands[ im.getNumBands()-1 ] =  _scoreBands[ im.getNumBands() - 2 ];

    //test
    //System.out.println("Test: before ordering");
     //SortScore();

    /////////////////////////////
     // pick best and re-compute correlation again
     double [] tempScoreBands = new double [im.getNumBands()];
     int [] tempOrderBands = new int[im.getNumBands()];


     int i;
     boolean signal = true;
     // setup the top ranked band
     int numRanked = 1;
     //int numBands = im.getNumBands() -1;

     // the last value is the maximum
     for(band=0, i=0;band<tempScoreBands.length;band++, i++){
       if(band == maxScoreBand){
         i--;
         tempScoreBands[tempScoreBands.length-1] = _scoreBands[band];
         tempOrderBands[tempScoreBands.length-1] = _orderBands[band];
       }else{
         tempScoreBands[i]= _scoreBands[band];
         tempOrderBands[i]= _orderBands[band];
       }
     }

     int bandLastRemoved = maxScoreBand;
     int b1, b2, b11, b21;
     double val2;
     // rank until at least 2 bands are left
     while(im.getNumBands() - numRanked >= 2){

       maxScore = _lim.MIN_DOUBLE;
       maxScoreBand = -1;
       b11= -1;
       val2 = 0.0;
       // compute measure for every pair of un-ranked and bandLastRemoved bands
       // the measure is always positive although the normalized correlation
       // detects positive and negative relationship
       for(band = 0; band < im.getNumBands()-numRanked; band ++){
            crossMean = myImStats.CrossMeanVal(im,tempOrderBands[band],bandLastRemoved);
            if( stdev[tempOrderBands[band]] > 0 && stdev[bandLastRemoved] > 0 ){
                val1 = Math.abs( crossMean - mean[tempOrderBands[band]]*mean[bandLastRemoved] );
                val1 /= (stdev[tempOrderBands[band]]*stdev[bandLastRemoved]);
                val2 = 100.0 * (1.0 - val1);
                //tempScoreBands.imageDouble[ band ] = 100.0 * (1.0 - val1);
                //tempScoreBands.imageDouble[ band ] = tempScoreBands.imageDouble[ tempScoreBands.sampPerPixel - numRanked ] * (1.0 - val1);
                //if(tempScoreBands.imageDouble[ band ] < _lim.EPSILON)
                //  tempScoreBands.imageDouble[ band ] = 0.0;
                if(val2 < _lim.EPSILON)
                  val2  = 0.0;

                //test
                //if (numRanked < 3){
                //  System.out.println("TEST: numRanked ="+numRanked+",val2="+val2+",tempScoreBands.imageDouble["+ band+" ]="+tempScoreBands.imageDouble[ band ]);
                //}

                tempScoreBands[ band ] += val2;
             }else{
               tempScoreBands[ band ] += 0.0;
             }

             // find new maximum score
             if(tempScoreBands[band] > maxScore){
               maxScore = tempScoreBands[band];
               maxScoreBand = tempOrderBands[band];
               b11 = band;
             }
       }// end of for(band)


       //test
       //if (numRanked < 3){
       //  System.out.println("TEST: numRanked ="+numRanked+",maxScore="+maxScore+",maxScoreband="+maxScoreBand+",b11="+b11);
       //}

       // reshuffle so that the last value moves to the end
       for(band=b11;band<tempScoreBands.length - 1 - numRanked;band++){
         tempScoreBands[band] = tempScoreBands[band+1];
         tempOrderBands[band] = tempOrderBands[band+1];
       }
       tempScoreBands[tempScoreBands.length - 1 - numRanked] = maxScore/(numRanked+1);
       tempOrderBands[tempScoreBands.length - 1 - numRanked] = maxScoreBand;

       bandLastRemoved = maxScoreBand;
       numRanked ++;

        /*      //test
       if (numRanked < 3){
         for(i=0;i<numRanked;i++){
           System.out.println("TEST: tempScoreBands.imageDouble["+i+"]="+tempScoreBands.imageDouble[tempScoreBands.sampPerPixel - 1 - i] );
           System.out.println("TEST: tempOrderBands.imageDouble["+i+"]="+tempOrderBands.imageInt[tempScoreBands.sampPerPixel - 1 - i] );
         }
       }
        */
     }// end of while loop

     //System.out.println("TEST:numRanked = "+numRanked);
     tempScoreBands[0] /= (numRanked);

     // copy final results into the internal ImageObjects
     for(band=0;band<tempScoreBands.length;band++){
       _scoreBands[band] = tempScoreBands[tempScoreBands.length-1 - band];

      // guarantee that the score values are in the descending order
       if(band>0){
         //if(_scoreBands[band] > _scoreBands[band-1]){
         //  _scoreBands[band] = _scoreBands[band-1];
         //}
          //take care of scaling, respectively, score values should be guaranteed to be in descending order
          _scoreBands[band] *= _scoreBands[band-1]/100;
       }

       _orderBands[band] = tempOrderBands[tempScoreBands.length-1 - band];
     }


     return true;
   }

  ////////////////////////////////////////////////////////////////

  private void ScaleScore(ImageObject im){
      //scale the value to 0 -100
      double scale=1.0;
      if(im.getTypeString().equalsIgnoreCase("BYTE")){
        scale = 100.0/(_lim.MAXPOS_BYTE * (double)im.getNumRows() * im.getNumCols());
      }
      if(im.getTypeString().equalsIgnoreCase("SHORT")){
        scale = 100.0/(_lim.MAXPOS_SHORT * (double)im.getNumRows() * im.getNumCols());
      }
      int band;
      double val;
      if(im.getTypeString().equalsIgnoreCase("DOUBLE")){
        val = _lim.MIN_DOUBLE;
        for(band = 0; band < _maxVal.length; band ++){
         if (val < _maxVal[band] ){
            val = _maxVal[band] ;
         }
        }
        scale = 100.0/( (double)im.getNumRows() * im.getNumCols());
        if(val > 0){
          scale /= val;
        }else{
          System.out.println("ERROR: the results could not be scaled");
          System.out.println("WARNING: max of all bands < =0 ");
          return;
        }
      }

     if(im.getTypeString().equalsIgnoreCase("BYTE") || im.getTypeString().equalsIgnoreCase("SHORT")
       || im.getTypeString().equalsIgnoreCase("DOUBLE") ){
        for(band = 0; band < im.getNumBands(); band ++){
          _scoreBands[ band ] *= scale;
       }
     }
/*
     if(im.getTypeString().equalsIgnoreCase("BYTE") || im.getTypeString().equalsIgnoreCase("SHORT")){
        for(band = 0; band < im.getNumBands(); band ++){
          _scoreBands[ band ] *= scale;
       }
     }else{
        //ImStats myImStats = new ImStats();
        //myImStats.MinMaxVal(im );
        if(im.getTypeString().equalsIgnoreCase("DOUBLE")){
          for(band = 0; band < im.getNumBands(); band ++){
            //val = myImStats.GetMaxVal().imageDouble[band] - myImStats.GetMinVal().imageDouble[band];

            //test
            if(_scoreBands[ band ] > _maxVal.imageDouble[band]*im.numrows*im.numcols){
              System.out.println("ERROR: score > allowed at band="+band+",score="+_scoreBands[ band ] );
              //System.out.println("ERROR:  scale="+scale+", max-min="+val);
              //System.out.println("ERROR:max="+myImStats.GetMaxVal().imageDouble[band]+",min="+myImStats.GetMinVal().imageDouble[band]);
              System.out.println("ERROR:  min and max: min="+_minVal.imageDouble[band]+",max="+_maxVal.imageDouble[band]);
              //System.out.println("ERROR: before score="+(_scoreBands[ band ]/(scale/val) ) );
              //System.out.println("ERROR:  scale/val="+(scale/val) );

              im.PrintImageObject();
            }

            if(_maxVal.imageDouble[band] != _lim.MIN_DOUBLE && _minVal.imageDouble[band] != _lim.MAX_DOUBLE){
              val = _maxVal.imageDouble[band];// - _minVal.imageDouble[band];
            }else{
              System.out.println("ERROR:  did not compute min and max: min="+_minVal.imageDouble[band]+",max="+_maxVal.imageDouble[band]);
              val = 0.0;
            }
            if(val > 0 ){
              //_scoreBands[ band ] *= (scale/val) ;
              _scoreBands[ band ] *= scale;
              _scoreBands[ band ] /= val;

             // if(_scoreBands[ band ] > 100.0){
               // System.out.println("ERROR: score > 100 at band="+band+",score="+_scoreBands[ band ] );
               // System.out.println("ERROR:  scale="+scale+", max-min="+val);
                //System.out.println("ERROR:max="+myImStats.GetMaxVal().imageDouble[band]+",min="+myImStats.GetMinVal().imageDouble[band]);
               // System.out.println("ERROR:  min and max: min="+_minVal.imageDouble[band]+",max="+_maxVal.imageDouble[band]);
               // System.out.println("ERROR: before score="+(_scoreBands[ band ]/(scale/val) ) );
               // System.out.println("ERROR:  scale/val="+(scale/val) );

              //  im.PrintImageObject();
              //}

            }else{
              System.out.println("WARNING: max - min < =0 at band="+band);
             //_scoreBands[ band ] *= scale;
             _scoreBands[ band ] =0.0;

            }

          }
        }else{
          System.out.println("ERROR: the results could not be scaled");
        }
     }
*/
  }// end of ScaleScore()

  public void SortScore(){
      // sort based the measures from max to min values
      // the larger the value of error the more important the band !!
      int temp,i,band;
      double tempEntropy;
      for(band = 0; band < _scoreBands.length; band ++){
        for(i = band+1; i < _scoreBands.length; i ++){
           if(_scoreBands[ band ] < _scoreBands[ i ] ){
             // swap
             temp =  _orderBands[band];
             _orderBands[band] =  _orderBands[i];
             _orderBands[i] = temp;

              tempEntropy =  _scoreBands[ band ];
             _scoreBands[ band ]  =  _scoreBands[ i ];
             _scoreBands[ i ]  = tempEntropy;
          }
        }
      }

  }

  public void SortOrder(){
      // sort based the measures from max to min values
      // the larger the value of error the more important the band !!
      int temp,i,band;
      double tempEntropy;
      for(band = 0; band < _orderBands.length; band ++){
        for(i = band+1; i < _orderBands.length; i ++){
           if(_orderBands[ band ] > _orderBands[ i ] ){
             // swap
             temp =  _orderBands[band];
             _orderBands[band] =  _orderBands[i];
             _orderBands[i] = temp;

              tempEntropy =  _scoreBands[ band ];
             _scoreBands[ band ]  =  _scoreBands[ i ];
             _scoreBands[ i ]  = tempEntropy;
          }
        }
      }

  }

  // end of the class
}
