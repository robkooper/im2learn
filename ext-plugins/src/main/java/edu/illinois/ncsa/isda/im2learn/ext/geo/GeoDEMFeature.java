package edu.illinois.ncsa.isda.im2learn.ext.geo;


import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.GeoConvert;
import edu.illinois.ncsa.isda.im2learn.core.datatype.*;
import edu.illinois.ncsa.isda.im2learn.core.geo.projection.Projection;

//import uiuc.DecisionTreeAnalyzer;


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

public class GeoDEMFeature {


  double [][] m_Slope;
  double [][] m_Curvature;
  double [][] m_CTI; // Compound Topologic Index
  short  [][] m_Height;
  int    [][] m_FlowAccumulation;
  int    [][] m_FlowDirection;
  int    [][] m_BFSLabel;
  int    [][] m_Aspect;
  int    [][] m_WatershedLabel;
  int         m_NumWatershed = -1;

  protected GeoConvert _geoConv = null;

  ImageObject _img_Slope = null;
  ImageObject _img_Aspect = null;
  ImageObject _img_Curvature = null;
  ImageObject _img_SlopeUncertainMask = null;
  ImageObject _img_AspectUncertainMask = null;
  ImageObject _img_CurvatureUncertainMask = null;
  ImageObject _img_CTI = null;


  ImageObject _img_FlowDirection = null;
  ImageObject _img_FlowAccum = null;
  ImageObject _img_Watershed = null;
  ImageObject _img_Sink      = null;
  ImageObject _img_FilledHeight = null;

  //MutableTableImpl _FeatureTable = null;

  Vector      _SourceCellList = null;
  Vector      _SpillCellList = null;
  Vector      _WatershedGraph = null;
  Vector      _CT             = null;

  short Flat, Sink, TotalFlat;

  static private Log logger = LogFactory.getLog(GeoDEMFeature.class);

  public class IndexPair extends Object
  {
    public int x,y;
    public IndexPair()
    {
      x = 0; y=0;
    }
 }

 public class Node extends Object
 {
   public int Label;
   boolean IsDone;
   short  Height;
   Vector OutEdges;
   public Node()
   {
     Label = -1;
     Height = -1;
     IsDone = false;
     OutEdges = new Vector();
   }
 }


 public class Edge extends Object implements Comparable
 {
   public int PourPointX,PourPointY;
   int Label1, Label2;
   Node nextNode;
   short Height;
   public Edge() {
     Label1 = Label2 = -1;
     Height = -1;
     nextNode = null;
   }

   public int compareTo(Object obj) {
     Edge Cell = (Edge) obj;
     if (Label1 < Cell.Label1)
       return -1;
     else if (Label1 == Cell.Label1) {
       if (Label2 < Cell.Label2)
         return -1;
       else if (Label2 == Cell.Label2)
         return 0;
       else
         return 1;
     }
     else
       return 1;
   }
   public void Print()
   {
     System.out.println("Pour Point : " + PourPointX + ", " + PourPointY);
     System.out.println("Label 1 : " + Label1 + "  Label 2 : " + Label2);
     System.out.println("Height : " + Height + "\n");
   }
 }

 public class EdgeComparator implements Comparator
 {
   public int compare(Object o1,
                   Object o2)
   {
     Edge e1 = (Edge) o1;
     Edge e2 = (Edge) o2;

     if (e1.Height < e2.Height)
       return -1;
     else if (e1.Height == e2.Height)
       return 0;
     else
       return 1;

    }

 }

  public class ColorIndex
  {
    public short r,g,b;
    public ColorIndex()
    {
      r=0; g=0; b=0;
    }
  }

  public class PriorityCell extends Object implements Comparable
  {
    public int x,y;
    double height;
    int    BFSLabel;
    public PriorityCell()
    {
      x = 0; y=0;
    }

    public int compareTo(Object obj)
    {
      PriorityCell Cell = (PriorityCell) obj;
      if  (height < Cell.height)
        return -1;
      else if (height > Cell.height)
        return 1;
      else if (height == Cell.height)
      {
        if (BFSLabel < Cell.BFSLabel)
          return -1;
        else if (BFSLabel > Cell.BFSLabel)
          return 1;
        else return 0;
      }

      return 0;
    }
  }

  public GeoDEMFeature() {
    m_FlowDirection = null;
    m_FlowAccumulation = null;
    m_CTI = null;
    m_Curvature = null;
    m_Slope = null;
    m_Aspect = null;
    Sink = -2;
    Flat = -3;
    TotalFlat = -4;
  }


  public ImageObject GetSlope(){
    return _img_Slope;
  }

  public ImageObject GetAspect() {
    return _img_Aspect;
  }

  public ImageObject GetCurvature()
  {
    return _img_Curvature;
  }

  public ImageObject GetFlowDirection()
  {
    return _img_FlowDirection;
  }

  public ImageObject GetFlowAccum()
  {
    return _img_FlowAccum;
  }

  public ImageObject GetWatershed()
  {
    return _img_Watershed;
  }

  public ImageObject GetSink()
  {
    return _img_Sink;
  }

  public ImageObject GetDepressionFilled()
 {
   return _img_FilledHeight;
 }

 public ImageObject GetCTI()
 {
   return _img_CTI;
 }



/*
  public void ConstructTable()
  {
    DoubleColumn Slope = new DoubleColumn();
    IntColumn Row = new IntColumn();
    IntColumn Col = new IntColumn();


    _FeatureTable.addColumn(Row);
    _FeatureTable.addColumn(Col);
    _FeatureTable.addColumn(Slope);

  }
 */

  public void ComputeSlope(ImageObject geoIm, int WinSize)
  {
    int nRows,nCols;
    nRows = geoIm.getNumRows();
    nCols = geoIm.getNumCols();


    m_Slope = new double[nCols][nRows];
    m_Aspect = new int[nCols][nRows];
    _img_Slope = new ImageObjectDouble(nRows, nCols, 1);
    _img_Aspect = new ImageObjectShort(nRows, nCols, 1);

    double [] p;   // 3x3 window mask
                   //   0 1 2
                   //   3 4 5
                   //   6 7 8


    double x_spacing,y_spacing;
    double dx,dy;  // partial derivative along x and y direction

    if (geoIm.getProperty(ImageObject.GEOINFO) != null)
    {
    	Projection geo = (Projection) geoIm.getProperty(ImageObject.GEOINFO);
        x_spacing = geo.GetColumnResolution();
        y_spacing = geo.GetRowResolution();

        GeoInformation geoInfo = new GeoInformation();

        _geoConv = new GeoConvert(geoInfo);
        Point2DDouble pts = new Point2DDouble(2);
        pts.ptsDouble[1] = geo.GetMaxWestLng();
        pts.ptsDouble[0] = geo.GetMaxSouthLat();
        Point2DDouble ptsConv = null;
        ptsConv = _geoConv.LatLng2UTMNorthingEasting(pts);
        pts.ptsDouble[1] += x_spacing;
        pts.ptsDouble[0] += y_spacing;
        Point2DDouble ptsConv1 = null;
        ptsConv1 = _geoConv.LatLng2UTMNorthingEasting(pts);

        // colum res
        x_spacing = Math.abs(ptsConv.ptsDouble[1] - ptsConv1.ptsDouble[1]);
        //row res
        y_spacing = Math.abs(ptsConv.ptsDouble[0] - ptsConv1.ptsDouble[0]);
    }
    else
    {
    	x_spacing = 1.0;
    	y_spacing = 1.0;
    }

    int nSize = WinSize;
    int index = 0;
    for (int y=0;y<nRows;y++)
      for (int x=0;x<nCols;x++)
      {
            p = getWindowLarge(geoIm,x,y,nSize);

            dx = ComputeDX(p,nSize,y_spacing);
            dy = ComputeDY(p,nSize,y_spacing);

            // calculate the partial derivative of dx, dy
            //dy =( (p[0] + 2.0*p[1] + p[2]) - (p[6]+2.0*p[7]+p[8]))/ (y_spacing*8);
            //dx =( (p[0] + 2.0*p[3] + p[6]) - (p[2]+2.0*p[5]+p[8]))/ (y_spacing*8);
            //logger.debug("dx = " + dx + "   dy = " + dy);

             double rise_run = Math.sqrt(dx*dx+dy*dy);
             m_Slope[x][y] = Math.atan(rise_run)*57.29578;
             _img_Slope.setDouble(index,m_Slope[x][y]);
             _img_Aspect.setShort(index,GetAspect(dx,dy));
             index++;
      }
  }

  double ComputeDX(double [] p, int nSize, double spacing)
  {
	  double dx = 0;
	  double left=0;
	  double right=0;

	  for (int x=0;x<nSize;x++)
	  {
		  if (x < nSize/2)
		  {
			  for (int y=0;y<nSize;y++)
			  {
				left+= p[y*nSize+x];
				if (y==nSize/2)
					left+=p[y*nSize+x];
			  }
		  }
		  else if (x > nSize/2)
		  {
			  for (int y=0;y<nSize;y++)
			  {
				right+= p[y*nSize+x];
				if (y==nSize/2)
					right+=p[y*nSize+x];
			  }
		  }
	  }
	  //logger.debug("left = " + left + "  right = " + right);

	  int nCount = (int)(nSize-1)*(nSize+1);
	  dx = (left - right)/(nCount*spacing);

	  return dx;
  }

  double ComputeDY(double [] p, int nSize, double spacing)
  {
	  double dx = 0;
	  double up=0;
	  double down=0;

	  for (int y=0;y<nSize;y++)
	  {
		  if (y < nSize/2)
		  {
			  for (int x=0;x<nSize;x++)
			  {
				up+= p[y*nSize+x];
				if (x==nSize/2)
					up+=p[y*nSize+x];
			  }
		  }
		  else if (y > nSize/2)
		  {
			  for (int x=0;x<nSize;x++)
			  {
				down+= p[y*nSize+x];
				if (x==nSize/2)
					down+=p[y*nSize+x];
			  }
		  }
	  }
	  int nCount = (int)(nSize-1)*(nSize+1);
	  dx = (up - down)/(nCount*spacing);

	  return dx;
  }

  double ComputeDDX(double [] p, int nSize, double spacing)
  {
	  double ddx = 0;
	  double left=0;
	  double right=0;
	  double center=0;

	  for (int x=0;x<nSize;x++)
	  {
		  if (x < nSize/2)
		  {
			  for (int y=0;y<nSize;y++)
			  {
				left+= p[y*nSize+x];

			  }
		  }
		  else if (x > nSize/2)
		  {
			  for (int y=0;y<nSize;y++)
			  {
				right+= p[y*nSize+x];

			  }
		  }
		  else if (x == nSize/2)
		  {
			  for (int y=0;y<nSize;y++)
			  	  center+= p[y*nSize+x];
		  }

	  }
	  int nCount = (int)(nSize*0.5)*(nSize);
	  double lAvg = left/nCount;
	  double rAvg = right/nCount;
	  double cAvg = center/nSize;


	  ddx = (lAvg + rAvg - 2*cAvg)/(2*spacing*spacing);

	  return ddx;
  }

  double ComputeDDY(double [] p, int nSize, double spacing)
  {
	  double ddy = 0;
	  double up=0;
	  double down=0;
	  double center=0;

	  for (int y=0;y<nSize;y++)
	  {
		  if (y < nSize/2)
		  {
			  for (int x=0;x<nSize;x++)
			  {
				up+= p[y*nSize+x];

			  }
		  }
		  else if (y > nSize/2)
		  {
			  for (int x=0;x<nSize;x++)
			  {
				down+= p[y*nSize+x];

			  }
		  }
		  else if (y == nSize/2)
		  {
			  for (int x=0;x<nSize;x++)
			  	  center+= p[y*nSize+x];
		  }

	  }
	  int nCount = (int)(nSize*0.5)*(nSize);
	  double lAvg = up/nCount;
	  double rAvg = down/nCount;
	  double cAvg = center/nSize;


	  ddy = (lAvg + rAvg - 2*cAvg)/(2*spacing*spacing);

	  return ddy;
  }


  protected short GetAspect(double dx,double dy)
  {
    short Aspect = 0;
    //  To-Do : Compute aspect from slope (gradient) direction
    //          aspect value is increasing clockwise, with 0 degree at north.
    //
    //   ---------> X+
    //   |
    //   |
    //   |
    //  \./
    //   Y+

   if (dx==0 && dy ==0)
     return -1;

   dy = -dy;

   double dAngle = 0.0;
   dAngle = Math.atan(dx/dy)*57.29578;


   if (dy <=0)
       dAngle+=180.0;


   if (dx <0 && dy>0)
       dAngle+=360.0;




   Aspect = (short) dAngle;

    //if (Aspect == -90)
    //    Aspect += 180;

    return Aspect;
  }




  public void ComputeCurvature(ImageObject geoIm, int WinSize)
  {
    int nRows,nCols;
    nRows = geoIm.getNumRows();
    nCols = geoIm.getNumCols();

    _img_Curvature = new ImageObjectDouble(nRows, nCols, 1);


    m_Curvature = new double[nCols][nRows]; // PS : mean curvature
    double [] p;   // 3x3 window mask
                   //   0 1 2
                   //   3 4 5
                   //   6 7 8


    double x_spacing,y_spacing;
    double D,E,F,G,H;  // coefficients of 4-degree xy polynomial
                 // used here for curvature calculation
    double ddx,ddy;
    short AspectAngle;

    if (geoIm.getProperty(ImageObject.GEOINFO) != null)
    {
    	Projection geo = (Projection) geoIm.getProperty(ImageObject.GEOINFO);

        x_spacing = geo.GetColumnResolution();
        y_spacing = geo.GetRowResolution();

        GeoInformation geoInfo = new GeoInformation();

        _geoConv = new GeoConvert(geoInfo);
        Point2DDouble pts = new Point2DDouble(2);
        pts.ptsDouble[1] = geo.GetMaxWestLng();
        pts.ptsDouble[0] = geo.GetMaxSouthLat();
        Point2DDouble ptsConv = null;
        ptsConv = _geoConv.LatLng2UTMNorthingEasting(pts);
        pts.ptsDouble[1] += x_spacing;
        pts.ptsDouble[0] += y_spacing;
        Point2DDouble ptsConv1 = null;
        ptsConv1 = _geoConv.LatLng2UTMNorthingEasting(pts);

        // colum res
        x_spacing = Math.abs(ptsConv.ptsDouble[1] - ptsConv1.ptsDouble[1]);
        //row res
        y_spacing = Math.abs(ptsConv.ptsDouble[0] - ptsConv1.ptsDouble[0]);
    }
    else
    {
    	x_spacing = 1.0;
    	y_spacing = 1.0;
    }





    int nSize = WinSize;
    int index = 0;
    for (int y=0;y<nRows;y++)
      for (int x=0;x<nCols;x++)
      {
            p = getWindowLarge(geoIm,x,y,nSize);

            /*
            // calculate the 2nd order partial derivative D,E
            D = ((p[3]+p[5])/2 - p[4])/(x_spacing*x_spacing);
            E = ((p[1]+p[7])/2 - p[4])/(y_spacing*y_spacing);
            F = (-p[0] + p[2] + p[6] - p[8]) /(4.0*y_spacing*x_spacing);
            G = (-p[3] + p[5] ) / (2.0*y_spacing);
            H = (p[1] - p[7] ) / (2.0*y_spacing);

            ddx = ((p[0]+p[3]+p[6])- (p[1]+p[4]+p[7])*2.0 + (p[2]+p[5]+p[8]))/3.0;
            ddy = ((p[0]+p[1]+p[2])- (p[3]+p[4]+p[5])*2.0 + (p[6]+p[7]+p[8]))/3.0;
            */

            ddx = ComputeDDX(p,nSize,y_spacing);
            ddy = ComputeDDY(p,nSize,y_spacing);



            //m_Curvature[x][y] = -(D+E)/2;

            //m_Curvature[x][y] = Math.sqrt(ddx*ddx+ddy*ddy);

            m_Curvature[x][y] = -(ddx + ddy)*0.5;

            AspectAngle = GetAspect(ddx,ddy);

            //m_Curvature[x][y] = -2.0*(D*G*G + E*H*H + F*G*H)/(G*G + H*H); // profile curvature

            //m_Curvature[x][y] = 2.0*(D*H*H + E*G*G + F*G*H) / (G*G + H*H) *100.0;  // platform curvature

            _img_Curvature.setDouble(index,m_Curvature[x][y]);
            //_img_Curvature.setDouble(index*2+1,AspectAngle);
            //System.out.println("Curvature : " + m_Curvature[x][y]);
            index++;
      }

  }


  public void ComputeCTI(ImageObject geoIm)
  {
    if (m_Slope == null)
      ComputeSlope(geoIm,3);

    if (m_FlowAccumulation == null)
      ComputeFlowAccumulation(geoIm);

    int nRows,nCols;
     nRows = geoIm.getNumRows();
     nCols = geoIm.getNumCols();


    _img_CTI = new ImageObjectDouble(nRows, nCols, 1);


    m_CTI = new double[nCols][nRows];
    int x,y;
    double dTemp;
    int index=0;
    for (y=0;y<nRows;y++)
      for (x=0;x<nCols;x++)
      {
        if (m_Slope[x][y] != 0)
        {
          dTemp = m_FlowAccumulation[x][y] / Math.tan(m_Slope[x][y]/57.29578);
          m_CTI[x][y] = Math.log(dTemp);
        }
        else
          m_CTI[x][y] = 0.0;


        if (m_CTI[x][y] < 0)
            m_CTI[x][y] = 0.0;

        _img_CTI.setDouble(index,m_CTI[x][y]);
        index++;
      }



  }

  protected ColorIndex NumberToColor(short Number)
  {
    ColorIndex c = new ColorIndex();
    c.r = 0; c.g = 0; c.b = 0;
    c.r = (short)((Number%40)*6);
    c.g = (short)(255 - (Number%8)*30);
    c.b = (short)((Number%50)*5 + (Number%2)*30);

    return c;
  }

  public void ComputeSinks(ImageObject geoIm)
  {
    int nRows,nCols;
      nRows = geoIm.getNumRows();
      nCols = geoIm.getNumCols();


   _img_Sink = new ImageObjectShort(nRows, nCols,1);

   ComputeFlowDirection(geoIm);

   FindSinkLabels(nRows,nCols);


  }


  public void FillDepression(ImageObject geoIm)
  {
    int nRows,nCols;
    nRows = geoIm.getNumRows();
    nCols = geoIm.getNumCols();

    FindWatershed(geoIm);
    ConstructWatershedGraph(nRows,nCols);

    /*
    System.out.println("\n\nPrinting Watershed Graph Edges : ");
    for (int i=0;i<_WatershedGraph.size();i++)
    {
      ((Edge)_WatershedGraph.get(i)).Print();
    }
    */


    SweepWatershedGraphForSinkFilling(nRows,nCols,_WatershedGraph);
    RaiseWaterLevel(nRows,nCols,_CT);
    _img_FilledHeight = new ImageObjectShort(nRows, nCols, 1);
    for (int y=0;y<nRows;y++)
      for (int x=0;x<nCols;x++)
      {
        geoIm.setShort(y*nCols+x,m_Height[x][y]);
        //_img_FilledHeight.imageShort[y*nCols+x] = m_Height[x][y];
      }
    this.ComputeFlowDirection(geoIm);

    for (int y=0;y<nRows;y++)
     for (int x=0;x<nCols;x++)
     {
       _img_FilledHeight.setShort(y*nCols+x,m_Height[x][y]);
     }


  }

  public void FindWatershed(ImageObject geoIm)
  {

    System.out.println("Watershed Begin");

    int nRows,nCols;
    nRows = geoIm.getNumRows();
    nCols = geoIm.getNumCols();


    ComputeSinks(geoIm);

    _img_Watershed = new ImageObjectShort(nRows, nCols,3);

    int [] WatershedPixelCounts = new int[m_NumWatershed];





    int i,j,x,y;
    int indexX,indexY;
    boolean bIsTop = true;

    for (i=0;i<m_NumWatershed;i++)
         WatershedPixelCounts[i] = 0;

    IndexPair Pair;

    int [][] DirCode = new int[3][3];
    // Pre-encoding the flowing direction in an array. To match if the adjacent cells have flow direction
    // into this cell.
    //
    // !Notice that the in-flow dir. is opposite to flow direction, since we try to match adjacent cells'
    //  "out-flow" direction into this cell.
    //     2    4     8
    //     1   cell  16
    //    128   64   32
    //
    DirCode[0][0] = 2;   DirCode[1][0] = 4;   DirCode[2][0] = 8;    DirCode[2][1] = 16;
    DirCode[2][2] = 32;  DirCode[1][2] = 64;  DirCode[0][2] = 128;  DirCode[0][1] = 1;
    ///////////////////////////////////////////////////////////////////////////////////////////////////////


    Vector SortedList = new Vector();
    for (y=0;y<nRows;y++)
      for (x=0;x<nCols;x++)
    {
      PriorityCell cell = new PriorityCell();
      cell.x = x;
      cell.y = y;
      cell.height =  m_Height[x][y];
      cell.BFSLabel = m_BFSLabel[x][y];
      SortedList.add(cell);

    }


    Collections.sort(SortedList);

    PriorityCell cell;
    IndexPair AdjPair;
    Vector AdjacentList = new Vector();
    for (int index=0;index<SortedList.size();index++)
    {
      cell = (PriorityCell) SortedList.get(index);

      int IDX = m_WatershedLabel[cell.x][cell.y];
      WatershedPixelCounts[IDX] += 1;


      ColorIndex c = NumberToColor((short)m_WatershedLabel[cell.x][cell.y]);



      _img_Watershed.setShort((cell.y*nCols+cell.x)*3, c.r) ;
      _img_Watershed.setShort((cell.y*nCols+cell.x)*3+1 , c.g) ;
      _img_Watershed.setShort((cell.y*nCols+cell.x)*3+2 , c.b) ;


      for (i=-1;i<=1;i++)
         for (j=-1;j<=1;j++)
         {
           indexX = j+cell.x;
           indexY = i+cell.y;

           if (i==0 && j==0)
             continue;

           if (indexX <0 || indexX >= nCols ||
               indexY <0 || indexY >= nRows)
             continue;
           else
           {
               if (m_FlowDirection[indexX][indexY] == DirCode[j+1][i+1] &&
                   m_WatershedLabel[indexX][indexY] == -1)
               // in-flow direction match, which means there is water flowing into this cell.
               // Also, need to check if there is already a watershed label assigned to the adjacent cell.
               {
                      m_WatershedLabel[indexX][indexY] = m_WatershedLabel[cell.x][cell.y];

               }
           }
         }
      }

      int maxcount = -1;
      for (i=0;i<m_NumWatershed;i++)
        if (maxcount < WatershedPixelCounts[i])
           maxcount = WatershedPixelCounts[i];

      System.out.println("Max Watershed Pixel Counts : " + maxcount);
}

  protected void ConstructWatershedGraph(int nRows,int nCols)
  {
    int x,y;
    Vector cList = new Vector();
    _WatershedGraph = new Vector();
    IndexPair Pair;
    Edge edge,oldedge;
    for (y=0;y<nRows;y++)
      for (x=0;x<nCols;x++)
      {
        //System.out.println("Current Pixel Height : " + m_Height[x][y]);

        if (y==0 || x==0 || y==nRows-1 || x==nRows-1)
        // if the watershed is on the boundary
        {
          edge = new Edge();
          edge.Height = m_Height[x][y];
          edge.PourPointX = x; edge.PourPointY = y;
          edge.Label1 =  m_WatershedLabel[x][y];
          edge.Label2 = m_NumWatershed; // special label for the outside watershed



            int iIndex = Collections.binarySearch(_WatershedGraph,edge);
            //System.out.println("Search Index : " + iIndex);
            if (iIndex < 0)
            {
              _WatershedGraph.add(edge);
              Collections.sort(_WatershedGraph);
            }
            else
            {
                oldedge = (Edge)_WatershedGraph.get(iIndex);
                //System.out.println("Replace oldedge");
                //edge.Print();
                //oldedge.Print();

                if (edge.Height < oldedge.Height)
                // only keep the edge of the lowest height
                {

                  //System.out.println("Edge Height : " + edge.Height);
                  _WatershedGraph.remove(iIndex);
                  _WatershedGraph.add(iIndex,edge);
                }
            }
        }

        // deal with internal vertex
        this.getWindowRightIndex(cList,x,y,nRows,nCols);
        for (int i=0;i<cList.size();i++)
        {
          Pair = (IndexPair) cList.get(i);
          if (m_WatershedLabel[Pair.x][Pair.y] != m_WatershedLabel[x][y])
          // if adjacent watershed labels are different, store it as an edge
          {
            edge = new Edge();
            if (m_Height[Pair.x][Pair.y] > m_Height[x][y])
            {
              edge.Height = m_Height[Pair.x][Pair.y];
              edge.PourPointX = Pair.x; edge.PourPointY = Pair.y;
            }
            else
            {
                edge.Height = m_Height[x][y];
                edge.PourPointX = x; edge.PourPointY = y;
            }
            if (m_WatershedLabel[Pair.x][Pair.y] < m_WatershedLabel[x][y])
            {
              edge.Label1 = m_WatershedLabel[Pair.x][Pair.y];
              edge.Label2 =  m_WatershedLabel[x][y];
            }
            else
            {
                edge.Label1 = m_WatershedLabel[x][y];
                edge.Label2 = m_WatershedLabel[Pair.x][Pair.y];
             }



             int iIndex = Collections.binarySearch(_WatershedGraph,edge);
             //System.out.println("Current Edge : ");
             //edge.Print();
             //System.out.println("Search Index : " + iIndex);
             if (iIndex < 0)
             {
               _WatershedGraph.add(edge);
               Collections.sort(_WatershedGraph);
             }
             else
             {
                 oldedge = (Edge)_WatershedGraph.get(iIndex);
                 //System.out.println("Replace oldedge");
                 //oldedge.Print();

                 if (edge.Height < oldedge.Height)
                 // only keep the edge of the lowest height
                 {
                   //System.out.println("Edge Height : " + edge.Height);
                    _WatershedGraph.remove(iIndex);
                    _WatershedGraph.add(iIndex,edge);
                 }
             }
           }
          }
       }
  }

  protected void ConstructFlowGraphFromWatershedGraph(int nRows,int nCols,Vector WatershedGraph)
  {
    Vector FlowGraph = new Vector();
    int i;
    for (i=0;i<m_NumWatershed;i++)
    {
      Node vtx = new Node();
      vtx.Label = i;
      FlowGraph.add(vtx);
    }

    for (i=0;i<WatershedGraph.size();i++)
    // construct the directed flow graph
    {
      Edge edge = (Edge) WatershedGraph.get(i);
      int iLabel = m_WatershedLabel[edge.PourPointX][edge.PourPointY];
      Node vtx = (Node) FlowGraph.get(iLabel);
      int iOutLabel;
      if (iLabel == edge.Label1)
        iOutLabel = edge.Label2;
      else
        iOutLabel = edge.Label1;

      edge.nextNode = (Node) FlowGraph.get(iOutLabel);
      vtx.OutEdges.add(edge);
     }
  }

  protected void SweepWatershedGraphForSinkFilling(int nRows,int nCols,Vector WatershedGraph)
  {
    Collections.sort(WatershedGraph,new EdgeComparator());
    _CT = new Vector();
    Vector CT = _CT;
    int i;
    for (i=0;i<m_NumWatershed;i++)
    {
      Node vtx = new Node();
      vtx.Label = i;
      CT.add(vtx);
    }
    Node vtx = new Node();
    vtx.Label = m_NumWatershed;
    vtx.IsDone = true;
    CT.add(vtx);

    for (i=0;i<WatershedGraph.size();i++)
    {
      Edge edge = (Edge) WatershedGraph.get(i);

      // find an edge (u,v)
      Node u,v;
      u = (Node) CT.get(edge.Label1);
      v = (Node) CT.get(edge.Label2);
      if (!u.IsDone && !v.IsDone)
      // if both u and v is not done yet
      {
        // add an edge to connect u,v
        Edge e1,e2;
        e1 = new Edge();
        e2 = new Edge();
        e1.nextNode = v;
        e2.nextNode = u;
        u.OutEdges.add(e1);
        v.OutEdges.add(e2);

      }
      else if (u.IsDone || v.IsDone)
      {
        int j;
        if (u.IsDone)
        {
          this.TraverseNode(v,edge.Height);

          /*
          for (j=0;j<v.OutEdges.size();j++)
          {
            Node NotDone = ((Edge)v.OutEdges.get(j)).nextNode;
            if (!NotDone.IsDone)
            {
              NotDone.IsDone = true;
              NotDone.Height = edge.Height;
            }
          }
          v.IsDone = true;
          v.Height = edge.Height;
          */
        }
        else // v.IsDone
        {
            this.TraverseNode(u,edge.Height);

        /*
          for (j=0;j<u.OutEdges.size();j++)
          {
            Node NotDone = ((Edge)u.OutEdges.get(j)).nextNode;
            if (!NotDone.IsDone)
            {
              NotDone.IsDone = true;
              NotDone.Height = edge.Height;
            }
          }
          u.IsDone = true;
          u.Height = edge.Height;
        */
       }
      }
      else if (u.IsDone && v.IsDone)
        continue;
    }
  }

  protected void TraverseNode(Node node,short height)
  {
    if (node.IsDone)
      return;
    else
    {
         node.IsDone = true;
         node.Height = height;
         for (int j=0;j<node.OutEdges.size();j++)
         {
           Node NotDone = ((Edge)node.OutEdges.get(j)).nextNode;
           TraverseNode(NotDone,height);

         }
    }
  }

 protected void RaiseWaterLevel(int nRows,int nCols,Vector CT)
 {
   for (int y=0;y<nRows;y++)
     for (int x=0;x<nCols;x++)
     {
       int iLabel = m_WatershedLabel[x][y];
       short Height = ((Node)CT.get(iLabel)).Height;
       if (m_Height[x][y] < Height)
         m_Height[x][y] = Height;
     }
}

 protected void RecursiveFillSinkLabel(IndexPair Pair,int nRows,int nCols,int iLabel)
 // bread first flood fill
 {
   int x,y;
   x = Pair.x; y = Pair.y;

   Vector PairList = new Vector();
   PairList.add(Pair);

   boolean bStop = false;

   Vector AdjacentList = new Vector();
   int iIndex = 0;
   IndexPair CurrentPair,AdjPair;
   while (iIndex < PairList.size())
   {
     AdjacentList.clear();
     CurrentPair = (IndexPair) PairList.get(iIndex);
     getWindowIndex(AdjacentList,CurrentPair.x,CurrentPair.y,nRows,nCols);
     //System.out.println("Current Pair : " + CurrentPair.x + ", " +CurrentPair.y);

     for (int i=0;i<AdjacentList.size();i++)
     {
       AdjPair = (IndexPair) AdjacentList.get(i);
       if (m_WatershedLabel[AdjPair.x][AdjPair.y] == -1
           && (m_FlowDirection[AdjPair.x][AdjPair.y] == Flat || m_FlowDirection[AdjPair.x][AdjPair.y] == TotalFlat))
       {
         m_WatershedLabel[AdjPair.x][AdjPair.y] = iLabel;
         PairList.add(AdjPair);
       }
     }

     iIndex++;
   }
 }
 protected void FindSinkLabels(int nRows,int nCols)
   {


     m_WatershedLabel = new int[nCols][nRows];
     Vector SinkList = new Vector();
     IndexPair Pair;
     int x,y;
     for (y=0;y<nRows;y++)
       for (x=0;x<nCols;x++)
       {
         if (m_FlowDirection[x][y] == Sink || m_FlowDirection[x][y] == Flat)
         // possible sinks
         {
           Pair = new IndexPair();
           Pair.x = x; Pair.y = y;
           SinkList.add(Pair);
           _img_Sink.setShort(y*nCols+x,(short)100);
         }
         m_WatershedLabel[x][y] = -1;
       }

     int iLabel = 0;
     for (int i=0;i<SinkList.size();i++)
     {

       Pair = (IndexPair) SinkList.get(i);
       //System.out.println("Current Sink Label : " + iLabel);

       if (m_WatershedLabel[Pair.x][Pair.y] != -1) // the sink cell has been assigned label
         continue;
       else
       {
           m_WatershedLabel[Pair.x][Pair.y] = iLabel;
           RecursiveFillSinkLabel(Pair,nRows,nCols,iLabel);
           iLabel += 1;
       }

     }
     m_NumWatershed = iLabel; // no of watersheds equal total number of labels
  }



  public void ComputeFlowDirection(ImageObject geoIm)
  {
    int nRows,nCols;
    nRows = geoIm.getNumRows();
    nCols = geoIm.getNumCols();


    m_FlowDirection = new int[nCols][nRows]; // PS : Flow Direction
    m_Height        = new short[nCols][nRows];
    _img_FlowDirection = new ImageObjectShort(nRows, nCols, 3);
    double [] p;   // 3x3 window mask
                   //   0 1 2
                   //   3 4 5
                   //   6 7 8
    double dCenter;
    double [] Zdiff; // z differences
    Zdiff = new double[9];

    double x_spacing,y_spacing;

    if (geoIm.getProperty(ImageObject.GEOINFO) != null)
    {
    	Projection geo = (Projection) geoIm.getProperty(ImageObject.GEOINFO);

        x_spacing = geo.GetColumnResolution();
        y_spacing = geo.GetRowResolution();

        GeoInformation geoInfo = new GeoInformation();

        _geoConv = new GeoConvert(geoInfo);
        Point2DDouble pts = new Point2DDouble(2);
        pts.ptsDouble[1] = geo.GetMaxWestLng();
        pts.ptsDouble[0] = geo.GetMaxSouthLat();
        Point2DDouble ptsConv = null;
        ptsConv = _geoConv.LatLng2UTMNorthingEasting(pts);
        pts.ptsDouble[1] += x_spacing;
        pts.ptsDouble[0] += y_spacing;
        Point2DDouble ptsConv1 = null;
        ptsConv1 = _geoConv.LatLng2UTMNorthingEasting(pts);

        // colum res
        x_spacing = Math.abs(ptsConv.ptsDouble[1] - ptsConv1.ptsDouble[1]);
        //row res
        y_spacing = Math.abs(ptsConv.ptsDouble[0] - ptsConv1.ptsDouble[0]);

    }
    else
    {
    	x_spacing = 1.0;
    	y_spacing = 1.0;
    }







    int index=0;
    int x,y;
    ColorIndex c;



    for (y=0;y<nRows;y++)
    for (x=0;x<nCols;x++)
    {
      m_Height[x][y] = geoIm.getShort(y * nCols + x);
    }

    double dNeighbor;

    Vector AdjList = new Vector();



    for (y=0;y<nRows;y++)
    for (x=0;x<nCols;x++)
    // fill single depression to avoid noise
    {
      AdjList.clear();
      boolean bSink = true;
      short iLowestNeighbor = 20000;
      getWindowIndex(AdjList,x,y,nRows,nCols);
      for (int i=0;i<AdjList.size();i++)
      {
          IndexPair Pair = (IndexPair) AdjList.get(i);
          if ( m_Height[x][y] > m_Height[Pair.x][Pair.y] )
          {
            bSink = false;
          }
          if (m_Height[Pair.x][Pair.y] < iLowestNeighbor)
            iLowestNeighbor = m_Height[Pair.x][Pair.y];
        }
      if (bSink)
      {
        m_Height[x][y] = iLowestNeighbor;
      }
    }

    for (y=0;y<nRows;y++)
    for (x=0;x<nCols;x++)
    {
       m_FlowDirection[x][y] = FindSteepestDescend(geoIm,x,y,1);
       c = DirectionToColor(m_FlowDirection[x][y]);
       _img_FlowDirection.setShort(index*3,c.r);
       _img_FlowDirection.setShort(index*3+1,c.g);
       _img_FlowDirection.setShort(index*3+2,c.b);
       index++;
    }

    Vector SpillList = new Vector();
    FindSpillCellList(SpillList,nRows,nCols);
    AssignFlowDirectionToPlateau(SpillList,nRows,nCols);

    /*
    int k = 1;
    for (int i=0;i<8;i++)
    {
      c = DirectionToColor(k);
      _img_FlowDirection.imageShort[index*3] = c.r;
      _img_FlowDirection.imageShort[index*3+1] = c.g;
      _img_FlowDirection.imageShort[index*3+2] = c.b;
      k=k*2;
      index++;
    }
      }
     */

 }
  protected short FindSteepestDescend(ImageObject geoIm,int x,int y,int iDimension)
  {
    short FlowDirection = -1;
    double dCenter = getBoundaryHeight(x,y,geoIm.getNumRows(),geoIm.getNumCols());
    double dNeighbor;
    double dLowestNeighbor = 10000.0;
    int    iLowestX=0,iLowestY=0;
    Vector iEqualX,iEqualY;
    iEqualX = new Vector(); iEqualY = new Vector();


    double dDrop = 0.0;
    int    iDropX=0,iDropY=0;
    double dMaxDrop = 10000.0;
    int iDropEqualCount = 0;
    int iCountEqual = 0;

    for (int i=-iDimension;i<=iDimension;i++)
      for (int j=-iDimension;j<=iDimension;j++)
      {
        if (i!=0 || j!=0)
        {
          dNeighbor = getBoundaryHeight(x+i,y+j,geoIm.getNumRows(),geoIm.getNumCols());
          if (i!=0 && j!=0)
          // diagonal direction
          {
            dDrop = (dNeighbor-dCenter)/1.414;
          }
          else
            dDrop = (dNeighbor-dCenter);

          if (dDrop < dMaxDrop)
          {
            iDropEqualCount = 0;
            dMaxDrop = dDrop;
            iEqualX.clear();
            iEqualY.clear();
            iDropX = i;
            iDropY = j;
          }
          if (dDrop == dMaxDrop)
          {
            iDropEqualCount++;
            iEqualX.add(new Integer(i));
            iEqualY.add(new Integer(j));
          }
          // Find the lowest neighboring point
          if (dNeighbor < dLowestNeighbor)
          {
            iCountEqual = 0;
            dLowestNeighbor = dNeighbor;
            iLowestX = i;
            iLowestY = j;
          }
          if (dNeighbor == dLowestNeighbor)
          // If there are multiple lowest neighbors, keep track of them
          // in the list
          {
            iCountEqual++;

          }
        }
      }


    if (dMaxDrop <= 0)
    // otherwise, set the flow direction toward the lowest neighbor cell
    {
        if (iDropEqualCount == 1 && dMaxDrop <0)
            FlowDirection = XYToDirection(iDropX,iDropY);
        else if (dMaxDrop <0)
        {
          // look up table
          FlowDirection = XYToDirection(iDropX,iDropY); // special case when all descents are equal
        }
        else if (dMaxDrop ==0 && iCountEqual < ((iDimension*2+1)*(iDimension*2+1) - 1) )
        // multiple equal elevation neighbors
        // sum up direction codes
        {
          FlowDirection = Flat;
        }
        else
        // all neighbor elevations are equal to center cell
        // special case as flat land
        {
            FlowDirection = TotalFlat;
        }
    }
    else if (dMaxDrop > 0)
    // if the center cell is lower than all neighbor cells
    // However, if we will fill the depression area, this should not be a possible case.
    {

      if ( x==0 || y==0 ||
           x==geoIm.getNumCols()-1 || y==geoIm.getNumRows()-1)
      // boundary point case, flow outside the boundary
      {
        if (x==0 && y==0)
          FlowDirection = 32;
        else if (x==0 && y==geoIm.getNumRows()-1)
          FlowDirection = 8;
        else if (x==0)
          FlowDirection = 16;
        else if (x==geoIm.getNumCols()-1 && y==0)
          FlowDirection = 128;
        else if (x==geoIm.getNumCols()-1 && y==geoIm.getNumRows()-1)
          FlowDirection = 2;
        else if (x==geoIm.getNumCols()-1)
          FlowDirection = 1;
        else if (y==0)
          FlowDirection = 64;
        else if (y==geoIm.getNumCols()-1)
          FlowDirection = 4;

          return FlowDirection;
      }

      if (iCountEqual == 1)
      // only 1 lowest neighbor, consider the center cell as noise
      {

        FlowDirection = Sink;//XYToDirection(iLowestX,iLowestY);
        //FlowDirection = 1;
      }
      else
      {
          //FlowDirection = 1;
          FlowDirection = Sink;

      }
    }

    return (short)(FlowDirection);
  }
  public void ComputeFlowAccumulation(ImageObject geoIm)
  {
    if (m_FlowDirection == null)
      ComputeFlowDirection(geoIm);

    int nRows,nCols;
     nRows = geoIm.getNumRows();
     nCols = geoIm.getNumCols();


    _img_FlowAccum = new ImageObjectInt(nRows, nCols, 1);


    m_FlowAccumulation = new int[nCols][nRows];
    int x,y;
    for (x=0;x<nCols;x++)
      for (y=0;y<nRows;y++)
        m_FlowAccumulation[x][y] = 0;



    _SourceCellList = new Vector();
    FindTopCellList(_SourceCellList,nRows,nCols); // find all cells that have no incoming flows
    IndexPair Pair;


    for (int i=0;i<_SourceCellList.size();i++)
    // for each top cell, trace a flow from it to computing accumulation along this flow
    {
      Pair = (IndexPair) _SourceCellList.get(i);
      FlowToDownHill(Pair,nRows,nCols);
     }

     int Threshold = 500;

     int index = 0;
     for (y=0;y<nRows;y++)
      for (x=0;x<nCols;x++)
      {
      if (m_FlowAccumulation[x][y] < Threshold)
       _img_FlowAccum.setInt(index,m_FlowAccumulation[x][y]);
      else
       _img_FlowAccum.setInt(index,Threshold);


       index++;
      }



 }
 protected void FlowToDownHill(IndexPair Sp, int nRows, int nCols)
 {
   int CurrentX,CurrentY;
   int CurrentAccumulation = 0;
   int CurrentDirection = m_FlowDirection[Sp.x][Sp.y];
   CurrentX = Sp.x; CurrentY = Sp.y;

   //System.out.println("Start x=" + CurrentX + "  Start y=" +CurrentY);

   boolean bStop = false;
   boolean bAccumulating = true;

   while ( IsValidDirection(CurrentDirection) && bStop == false)
   {
     IndexPair FlowXY = DirectionToXY(CurrentDirection);
     //System.out.println("Flow x=" + FlowXY.x + "  Flow y=" +FlowXY.y);
     CurrentX = CurrentX + FlowXY.x;
     CurrentY = CurrentY + FlowXY.y;


     if (CurrentX <0 || CurrentX >=nCols || CurrentY <0 || CurrentY >=nRows)
     // if flowing outside the grid, stop flowing.
        bStop = true;
     else {

       if (bAccumulating) CurrentAccumulation++;

       if (m_FlowAccumulation[CurrentX][CurrentY] != 0)
         bAccumulating = false;

       m_FlowAccumulation[CurrentX][CurrentY] += CurrentAccumulation;
       CurrentDirection = m_FlowDirection[CurrentX][CurrentY];
       //System.out.println("Current x=" + CurrentX + "  Current y=" +CurrentY);
       //System.out.println("Current Accum " + m_FlowAccumulation[CurrentX][CurrentY]);

     }
   }
 }



  protected void FindTopCellList(Vector cList,int nRows,int nCols)
  {
    int i,j,x,y;
    int indexX,indexY;
    boolean bIsTop = true;

    IndexPair Pair;

    int [][] DirCode = new int[3][3];
    // Pre-encoding the flowing direction in an array. To match if the adjacent cells have flow direction
    // into this cell.
    //
    // !Notice that the in-flow dir. is opposite to flow direction, since we try to match adjacent cells'
    //  "out-flow" direction into this cell.
    //     2    4     8
    //     1   cell  16
    //    128   64   32
    //
    DirCode[0][0] = 2;   DirCode[1][0] = 4;   DirCode[2][0] = 8;    DirCode[2][1] = 16;
    DirCode[2][2] = 32;  DirCode[1][2] = 64;  DirCode[0][2] = 128;  DirCode[0][1] = 1;
    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    for (y=0;y<nRows;y++)
      for (x=0;x<nCols;x++)
      {
        bIsTop = true; // assume this cell is top
        for (i=-1;i<=1;i++)
          for (j=-1;j<=1;j++)
          {
            indexX = j+x;
            indexY = i+y;

            if (i==0 && j==0)
              continue;

            if (indexX <0 || indexX >= nCols ||
                indexY <0 || indexY >= nRows)
              continue;
            else
            {
                if (m_FlowDirection[indexX][indexY] == DirCode[j+1][i+1])
                // in-flow direction match, which means there is water flowing into this cell.
                // Thus the cell is not top to its adjacent cells
                    bIsTop = false;
            }



          }
        if (bIsTop)
        {
          Pair = new IndexPair();
          Pair.x = x;
          Pair.y = y;
          cList.add(Pair);
          //System.out.println("Top Cell : " + Pair.x + ", " + Pair.y);
        }
      }
  }
 protected void FindSpillCellList(Vector cList,int nRows,int nCols)
 {
   int i,j,x,y;
   int indexX,indexY;
   boolean bIsSpill = false;

   IndexPair Pair;

   int [][] DirCode = new int[3][3];
   // Pre-encoding the flowing direction in an array. To match if the adjacent cells have flow direction
   // into this cell.
   //
   // !Notice that the in-flow dir. is opposite to flow direction, since we try to match adjacent cells'
   //  "out-flow" direction into this cell.
   //     2    4     8
   //     1   cell  16
   //    128   64   32
   //
   DirCode[0][0] = 2;   DirCode[1][0] = 4;   DirCode[2][0] = 8;    DirCode[2][1] = 16;
   DirCode[2][2] = 32;  DirCode[1][2] = 64;  DirCode[0][2] = 128;  DirCode[0][1] = 1;
   ///////////////////////////////////////////////////////////////////////////////////////////////////////

   for (y=0;y<nRows;y++)
     for (x=0;x<nCols;x++)
     {
       bIsSpill = false; // assume this cell is top
       if (!IsValidDirection(m_FlowDirection[x][y]))
           continue;

       for (i=-1;i<=1;i++)
         for (j=-1;j<=1;j++)
         {
           indexX = j+x;
           indexY = i+y;

           if (i==0 && j==0)
             continue;

           if (indexX <0 || indexX >= nCols ||
               indexY <0 || indexY >= nRows)
             continue;
           else
           {
               if (m_FlowDirection[indexX][indexY] == Flat && m_Height[indexX][indexY] == m_Height[x][y])
               {
                 bIsSpill = true;
               }


           }
        }


       if (bIsSpill)
       {
         Pair = new IndexPair();
         Pair.x = x;
         Pair.y = y;
         cList.add(Pair);
         //System.out.println("Spill Cell : " + Pair.x + ", " + Pair.y);
       }
     }
 }
 protected void AssignFlowDirectionToPlateau(Vector SpillList,int nRows,int nCols)
 {

      IndexPair Pair;
      Vector PairList = new Vector();
      Vector AdjacentList = new Vector();
      m_BFSLabel = new int[nCols][nRows];

      for (int y=0;y<nRows;y++)
        for (int x=0;x<nCols;x++)
          m_BFSLabel[x][y] = 0;

      for (int i=0;i<SpillList.size();i++)
      {
         Pair = (IndexPair) SpillList.get(i);
         PairList.add(Pair);
      }
      RecursiveBFS(PairList,nRows,nCols);
 }
 protected void RecursiveBFS(Vector PairList,int nRows,int nCols)
 {

   boolean bStop = false;

   Vector NewPairList = new Vector();
   Vector AdjacentList = new Vector();
   int iIndex = 0;
   IndexPair CurrentPair,AdjPair;
   int iDepth = 1;
   ColorIndex c;

  while (!bStop)
  {

   while (iIndex < PairList.size())
   {
     AdjacentList.clear();
     CurrentPair = (IndexPair) PairList.get(iIndex);
     getWindowIndex(AdjacentList,CurrentPair.x,CurrentPair.y,nRows,nCols);

     for (int i=0;i<AdjacentList.size();i++)
     {
       AdjPair = (IndexPair) AdjacentList.get(i);
       if (m_Height[AdjPair.x][AdjPair.y] == m_Height[CurrentPair.x][CurrentPair.y]
           && (m_FlowDirection[AdjPair.x][AdjPair.y] == Flat || m_FlowDirection[AdjPair.x][AdjPair.y] == TotalFlat))
       {
         //System.out.println("BFS Filling Plateau, Cell : " + AdjPair.x + ", " + AdjPair.y);
         m_FlowDirection[AdjPair.x][AdjPair.y] = XYToDirection(CurrentPair.x-AdjPair.x,CurrentPair.y-AdjPair.y);

         c = DirectionToColor(m_FlowDirection[AdjPair.x][AdjPair.y]);
         _img_FlowDirection.setShort((AdjPair.y*nCols+AdjPair.x)*3,c.r);
         _img_FlowDirection.setShort((AdjPair.y*nCols+AdjPair.x)*3+1,c.g);
         _img_FlowDirection.setShort((AdjPair.y*nCols+AdjPair.x)*3+2,c.b);
         NewPairList.add(AdjPair);
         m_BFSLabel[AdjPair.x][AdjPair.y] = iDepth;
       }
     }
     iIndex++;
   }

   if (NewPairList.isEmpty())
     bStop = true;
   PairList = NewPairList;
   NewPairList = new Vector();
   NewPairList.clear();
   iIndex = 0;
   iDepth++;
  }
 }










 protected boolean IsValidDirection(int Dir)
  {
    if ( Dir == 1 || Dir ==2 || Dir ==4 || Dir ==8 || Dir == 16 ||
         Dir == 32 || Dir == 64 || Dir == 128)
      return true;
    else
      return false;
  }


  public static double getBoundaryValue(ImageObject image, int x, int y, int band)
  {
    int ix,iy;
    ix = x; iy = y;

      if (ix < 0) ix = 0;
      if (ix >= image.getNumCols()) ix = image.getNumCols() - 1;
      if (iy < 0) iy = 0;
      if (iy >= image.getNumRows()) iy = image.getNumRows() - 1;
      //System.out.println("iX : " + ix + "  iY :" +iy);

      return image.getDouble((iy*image.getNumCols() + ix)*image.getNumBands() + band -1);

  }
  public double getBoundaryHeight(int x,int y,int nRows,int nCols)
  {
    int ix,iy;
    ix = x; iy = y;

      if (ix < 0) ix = 0;
      if (ix >= nCols) ix = nCols - 1;
      if (iy < 0) iy = 0;
      if (iy >= nRows) iy = nRows - 1;
      //System.out.println("iX : " + ix + "  iY :" +iy);

      return (double) m_Height[ix][iy];

  }
  public static byte getValue(ImageObject image, int x, int y, int band) {
    if(image.getType() == ImageObject.TYPE_BYTE) {
      return image.getByte((y*image.getNumCols() + x)*image.getNumBands() + band -1);
    }
    return 0;
  }
  public static double getValueDouble(ImageObject image, int x, int y, int band) {
    if(image.getType() == ImageObject.TYPE_DOUBLE) {
      return image.getDouble((y*image.getNumCols() + x)*image.getNumBands() + band -1);
    }
    return 0;
  }
  public static double getValueFloat(ImageObject image, int x, int y, int band) {
     if(image.getType() == ImageObject.TYPE_FLOAT) {
       return image.getFloat((y*image.getNumCols() + x)*image.getNumBands() + band -1);
     }
     return 0;
   }
  public static double getValueShort(ImageObject image, int x, int y, int band) {
      if(image.getType() == ImageObject.TYPE_SHORT) {
        return image.getShort((y*image.getNumCols() + x)*image.getNumBands() + band -1);
      }
      return 0;
    }
    public double [] getWindow(ImageObject image, int x, int y)
     {
       double [] p;
       p = new double[9];

       int nRows,nCols;
       nRows = image.getNumRows();
       nCols = image.getNumCols();

       int index = 0;
       for (int i=-1;i<=1;i++)
       {
         for (int j = -1; j <= 1; j++) {
           p[index] = getBoundaryValue(image,x+j,y+i,1);

           index++;
         }
       }

       return p;
     }

    public double [] getWindowLarge(ImageObject image, int x, int y, int nSize)
    {
      double [] p;
      p = new double[nSize*nSize];

      int nRows,nCols;
      nRows = image.getNumRows();
      nCols = image.getNumCols();

      int index = 0;
      int nWin = nSize/2;
      for (int i=-nWin;i<=nWin;i++)
      {
        for (int j = -nWin; j <= nWin; j++) {
          p[index] = getBoundaryValue(image,x+j,y+i,1);

          index++;
        }
      }

      return p;
    }

     public void getWindowIndex(Vector cList, int x,int y,int nRows,int nCols)
     {

       int indexX,indexY;
       IndexPair Pair;
       cList.clear();
       for (int i=-1;i<=1;i++)
       {
         for (int j = -1; j <= 1; j++) {
           if (i==0 && j==0)
             continue;

           indexX = x+i; indexY = y+j;
           if (indexX <0 || indexX >=nCols || indexY<0 || indexY>=nRows)
             continue;
           Pair = new IndexPair();
           Pair.x = indexX;
           Pair.y = indexY;
           cList.add(Pair);
         }
       }
     }
     public void getWindowRightIndex(Vector cList, int x,int y,int nRows,int nCols)
     {
       //  Consider only this 4 neighbors, for efficiency of some operations
       //       1
       //     0 2
       //     4 3


       int indexX,indexY;
       IndexPair Pair;
       cList.clear();
       for (int i=0;i<=1;i++)
       {
         for (int j = 0; j <= 1; j++) {
           if (i==0 && j==0)
             continue;

           indexX = x+i; indexY = y+j;
           if (indexX <0 || indexX >=nCols || indexY<0 || indexY>=nRows)
             continue;
           Pair = new IndexPair();
           Pair.x = indexX;
           Pair.y = indexY;
           cList.add(Pair);
         }
       }
       // special case for up-right direction
       indexX = x+1; indexY = y-1;
       if (indexX >=0 && indexX <nCols && indexY>=0 && indexY<nRows)
       {
           Pair = new IndexPair();
           Pair.x = indexX;
           Pair.y = indexY;
           cList.add(Pair);
       }

     }
     protected short XYToDirection(int x,int y)
 {
   if (x>0 && y==0)
     return 1;
   else if (x>0 && y>0)
     return 2;
   else if (x==0 && y>0)
     return 4;
   else if (x<0 && y>0)
     return 8;
   else if (x<0 && y==0)
     return 16;
   else if (x<0 && y<0)
     return 32;
   else if (x==0 && y<0)
     return 64;
   else if (x > 0 && y<0)
     return 128;

   return -1;
 }
 protected ColorIndex DirectionToColor(int DirIndex)
 {
   ColorIndex c = new ColorIndex();
   c.r = 0; c.g = 0; c.b = 0;

   if (DirIndex == 1)
   {
     c.r = 0; c.g = 100; c.b = 0;
   }
   else if (DirIndex == 2)
   {
     c.r = 100; c.g = 0; c.b = 0;
   }
   else if (DirIndex == 4)
   {
     c.r = 0; c.g = 0; c.b = 100;
   }
   else if (DirIndex == 8)
   {
     c.r = 139; c.g = 69; c.b = 19;
   }
   else if (DirIndex == 16)
   {
     c.r = 127; c.g = 255; c.b = 0;
   }
   else if (DirIndex == 32)
   {
     c.r = 255; c.g = 20; c.b = 147;
   }
   else if (DirIndex == 64)
   {
     c.r = 135; c.g = 206; c.b = 250;
   }
   else if (DirIndex == 128)
   {
     c.r = 255; c.g = 193; c.b = 193;
   }


   return c;
 }
 protected IndexPair DirectionToXY(int DirIndex)
 {
   IndexPair Pair = new IndexPair();
   if (DirIndex == 1)
   {
     Pair.x = 1; Pair.y = 0;
   }
   else if (DirIndex == 2)
   {
     Pair.x = 1; Pair.y = 1;
   }
   else if (DirIndex == 4)
   {
     Pair.x = 0; Pair.y = 1;
   }
   else if (DirIndex == 8)
   {
     Pair.x = -1; Pair.y = 1;
   }
   else if (DirIndex == 16)
   {
     Pair.x = -1; Pair.y = 0;
   }
   else if (DirIndex == 32)
   {
     Pair.x = -1; Pair.y = -1;
   }
   else if (DirIndex == 64)
   {
     Pair.x = 0; Pair.y = -1;
   }
   else if (DirIndex == 128)
   {
     Pair.x = 1; Pair.y = -1;
   }

   return Pair;
 }











}
