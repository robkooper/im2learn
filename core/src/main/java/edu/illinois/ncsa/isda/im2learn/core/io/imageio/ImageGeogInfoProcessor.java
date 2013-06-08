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
package edu.illinois.ncsa.isda.im2learn.core.io.imageio;

//import ncsa.im2learn.core.io.ImageLoader;
//import ncsa.im2learn.core.io.ImageReader;
//import ncsa.im2learn.core.io.ImageWriter;
//import ncsa.im2learn.core.io.shapefile.prj.ProjectionLoaderPRJ;
//
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//
//import javax.imageio.ImageIO;
//import javax.imageio.ImageReadParam;
//import javax.imageio.stream.ImageInputStream;
//import javax.imageio.stream.ImageOutputStream;
//import java.awt.image.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
//import java.util.Iterator;

import edu.illinois.ncsa.isda.im2learn.core.datatype.*;
import edu.illinois.ncsa.isda.im2learn.core.geo.projection.*;

public class ImageGeogInfoProcessor  {
  
  public ImageGeogInfoProcessor() {
    
  }
  
  /**
   * This method extracts the projection/geographic information from an ImageObject and
   * writes it to a human readable text file with the suffix .geog.txt . This text file
   * can be restored to a Projection object by the accompanying method readProjectionInfoFromFile().
   * 
   * @param imageToConsider the ImageObject from which to extract the information
   * @param basePathToWrite the path and filename (without the .geog.txt) to store the information in
   */
  public void writeRasterInfoToFile(ImageObject imageToConsider, String basePathToWrite) {
    Projection geogInfo = (Projection)imageToConsider.getProperty(ImageObject.GEOINFO);

    this.writeProjectionInfoToFile(geogInfo, basePathToWrite);
    
  }

  /**
   * This method extracts the projection/geographic information from a Projection and
   * writes it to a human readable text file with the suffix .geog.txt . This text file
   * can be restored to a Projection object by the accompanying method readProjectionInfoFromFile().
   * 
   * @param projectionToWrite the Projection from which to extract the parameters
   * @param basePathToWrite the path and filename (without the .geog.txt) to store the information in
   */
  public void writeProjectionInfoToFile(Projection projectionToWrite, String basePathToWrite) {

//    Projection geogInfo = (Projection)imageToConsider.getProperty(ImageObject.GEOINFO);
    
    String rasterString = new String("");
    
    rasterString += projectionToWrite.getEllipsoid() + " = Ellipsoid Index\n";
    rasterString += projectionToWrite.getRasterSpaceI() + " = RasterSpaceI\n";
    rasterString += projectionToWrite.getRasterSpaceJ() + " = RasterSpaceJ\n";
    rasterString += projectionToWrite.getRasterSpaceK() + " = RasterSpaceK\n";
    rasterString += projectionToWrite.getInsertionX() + " = InsertionX\n";
    rasterString += projectionToWrite.getInsertionY() + " = InsertionY\n";
    rasterString += projectionToWrite.getInsertionZ() + " = InsertionZ\n";
    rasterString += projectionToWrite.getScaleX() + " = ScaleX\n";
    rasterString += projectionToWrite.getScaleY() + " = ScaleY\n";
    rasterString += projectionToWrite.getScaleZ() + " = ScaleZ\n";
    rasterString += projectionToWrite.getNumRows() + " = NumRows\n";
    rasterString += projectionToWrite.getNumCols() + " = NumCols\n";
    rasterString += "\n";
    
    String projectionString = projectionToWrite.getProjectionParametersAsString();
    
    File geogOutputFile = new File(basePathToWrite + ".geog.txt");
    
    try {
      FileWriter outGeogStream = new FileWriter(geogOutputFile);
      PrintWriter outGeogWriterObject = new PrintWriter(outGeogStream);
      
      outGeogWriterObject.print(rasterString);
      outGeogWriterObject.print(projectionString);
      
      outGeogWriterObject.flush();
      outGeogWriterObject.close();
      outGeogStream.close();
      
    } catch (IOException ioe) {
      System.err.println("ERROR: trouble writing geographic information");
      ioe.printStackTrace();
    }
    
   
  }
  
  /**
   * This method creates a Projection object that is reconstructed from parameters
   * stored in a *.geog.txt file written by the writeRasterInfoToFile() or
   * writeProjectionInfoToFile() methods.
   * 
   * @param pathToRead the path and filename to read (including the .geog.txt suffix)
   * @return the appropriate projection with the parameters set to match those in the file
   */
  public Projection readProjectionInfoFromFile(String pathToRead) {
    
    int ellipsoidIndex = -5,
    nRows = -5,
    nCols = -6;
    double rsI = Double.NaN,
    rsJ = Double.NaN,
    rsK = Double.NaN,
    iX = Double.NaN,
    iY = Double.NaN,
    iZ = Double.NaN,
    sX = Double.NaN,
    sY = Double.NaN,
    sZ = Double.NaN;
    
    String lineContents = null,
    realGoodies = null,
    projInfoString = new String();
    Projection tempProjection = null;
    int projTypeInt = -1824;
    String projTypeString = null;
    String projTypeClassString = null;
    
    String magicDelimiter = " = ";
    
    File geogFileObject = new File(pathToRead);
    //sanity check
    if(geogFileObject == null){
  	  System.err.println("ERROR: file="+pathToRead+" does not exist");
  	  return null;
    }
    
    try {
      
      FileReader geogStream = new FileReader(geogFileObject);
      BufferedReader geogReader = new BufferedReader(geogStream);
      
      lineContents = geogReader.readLine();
      //sanity check
      if(lineContents == null){
    	  System.err.println("ERROR: the file does not contain a line");
    	  return null;
      }
      realGoodies = lineContents.substring(0,lineContents.indexOf(magicDelimiter));
      ellipsoidIndex = (Integer.parseInt(realGoodies));
      
      lineContents = geogReader.readLine(); realGoodies = lineContents.substring(0,lineContents.indexOf(magicDelimiter));
      rsI = (Double.parseDouble(realGoodies));
      
      lineContents = geogReader.readLine(); realGoodies = lineContents.substring(0,lineContents.indexOf(magicDelimiter));
      rsJ = (Double.parseDouble(realGoodies));
      
      lineContents = geogReader.readLine(); realGoodies = lineContents.substring(0,lineContents.indexOf(magicDelimiter));
      rsK = (Double.parseDouble(realGoodies));
      
      
      lineContents = geogReader.readLine(); realGoodies = lineContents.substring(0,lineContents.indexOf(magicDelimiter));
      iX = (Double.parseDouble(realGoodies));
      
      lineContents = geogReader.readLine(); realGoodies = lineContents.substring(0,lineContents.indexOf(magicDelimiter));
      iY = (Double.parseDouble(realGoodies));
      
      lineContents = geogReader.readLine(); realGoodies = lineContents.substring(0,lineContents.indexOf(magicDelimiter));
      iZ = (Double.parseDouble(realGoodies));
      
      
      lineContents = geogReader.readLine(); realGoodies = lineContents.substring(0,lineContents.indexOf(magicDelimiter));
      sX = (Double.parseDouble(realGoodies));
      
      lineContents = geogReader.readLine(); realGoodies = lineContents.substring(0,lineContents.indexOf(magicDelimiter));
      sY = (Double.parseDouble(realGoodies));
      
      lineContents = geogReader.readLine(); realGoodies = lineContents.substring(0,lineContents.indexOf(magicDelimiter));
      sZ = (Double.parseDouble(realGoodies));
      
      
      lineContents = geogReader.readLine(); realGoodies = lineContents.substring(0,lineContents.indexOf(magicDelimiter));
      nRows = (Integer.parseInt(realGoodies));
      
      lineContents = geogReader.readLine(); realGoodies = lineContents.substring(0,lineContents.indexOf(magicDelimiter));
      nCols = (Integer.parseInt(realGoodies));
      
      lineContents = geogReader.readLine(); // the blank line...
      
      lineContents = geogReader.readLine(); // the projection type line.
      if (lineContents.equalsIgnoreCase("null")) {
    	  // this means that it was geographic... so just assign the values and skip to the bottom...
    	  projTypeInt = Projection.GEOGRAPHIC;
      } else {
    	  // there is actually a projection in here, so read it.

    	  projTypeString = lineContents.substring(0,lineContents.indexOf(magicDelimiter));
    	  projTypeClassString = lineContents.substring(lineContents.indexOf(magicDelimiter) + magicDelimiter.length());
    	  projTypeInt = Integer.parseInt(projTypeString);

    	  // store the entire projection type line in the string holding the projection specific info
    	  projInfoString += (lineContents + "\n");
    	  // read the remaining lines in the file into the projection specific info string
    	  // i think we need to put newlines back in because they are stripped by readLine();
    	  while (true) {
    		  lineContents = geogReader.readLine();
    		  if (lineContents != null) {
    			  projInfoString += (lineContents + "\n");
    		  } else {
    			  break;
    		  }
    	  }
      }
    } catch (IOException ioe) {
      System.err.println("ERROR: problem reading from text file");
      ioe.printStackTrace();
    }
    
    // look for which projection we are trying to restore.
    switch (projTypeInt) {
    case Projection.ALASKA_CONFORMAL:
      tempProjection = new AlaskaConformal();
      tempProjection.setProjectionParametersFromString(projInfoString);
      break;
    case Projection.ALBERS_EQUAL_AREA_CONIC:
      // need to see more to figure out if we want the new or old...
//      System.err.println("___ we are in the Albers part of the switch; deciding new or old ____");
//      System.out.println("projTypeString = [" + projTypeClassString + "]");
//      System.out.println("intended logic true means old: projTypeString.indexOf(\"New\") < 0 [" 
//          + projTypeClassString.indexOf("New") + " < 0 ruling is" + (projTypeClassString.indexOf("New") < 0));
//      System.err.println("_________");
      if (projTypeClassString.indexOf("New") < 0) {
        // we have the old style projection object
        tempProjection = new AlbersEqualAreaConic();
        tempProjection.setProjectionParametersFromString(projInfoString);
      } else {
        // we have the new style projection object
        tempProjection = new NewAlbersEqualAreaConic();
        tempProjection.setProjectionParametersFromString(projInfoString);
      }
      break;
    case Projection.AZIMUTHAL_EQUIDISTANT:
      tempProjection = new AzimuthalEquidistant();
      tempProjection.setProjectionParametersFromString(projInfoString);
      break;
    case Projection.EQUIDISTANT_CONIC:
      tempProjection = new EquidistantConic();
      tempProjection.setProjectionParametersFromString(projInfoString);
      break;
    case Projection.EQUIRECTANGULAR:
      tempProjection = new Equirectangular();
      tempProjection.setProjectionParametersFromString(projInfoString);
      break;
    case Projection.GENERAL_VERTICAL_NEARSIDE_PERSPECTIVE:
      tempProjection = new GeneralVerticalNearSidePerspective();
      tempProjection.setProjectionParametersFromString(projInfoString);
      break;
    case Projection.GEOGRAPHIC:
      // we are good, don't do anything. leave it as just a Projection();
      tempProjection = new Projection();
      break;
    case Projection.GNOMONIC:
      tempProjection = new Gnomonic();
      tempProjection.setProjectionParametersFromString(projInfoString);
      break;
    case Projection.HAMMER:
      tempProjection = new Hammer();
      tempProjection.setProjectionParametersFromString(projInfoString);
      break;
    case Projection.INTEGERIZED_SINUSOIDAL:
      tempProjection = new IntegerizedSinusoidal();
      tempProjection.setProjectionParametersFromString(projInfoString);
      break;
    case Projection.INTERRUPTED_GOODE_HOMOLOSINE:
      tempProjection = new InterruptedGoodeHomolosine();
      tempProjection.setProjectionParametersFromString(projInfoString);
      break;
    case Projection.INTERRUPTED_MOLLWEIDE:
      tempProjection = new InterruptedMollweide();
      tempProjection.setProjectionParametersFromString(projInfoString);
      break;
    case Projection.LAMBERT_AZIMUTHAL_EQUAL_AREA:
      tempProjection = new LambertAzimuthalEqualArea();
      tempProjection.setProjectionParametersFromString(projInfoString);
      break;
    case Projection.LAMBERT_CONFORMAL_CONIC:
      tempProjection = new LambertConformalConic();
      tempProjection.setProjectionParametersFromString(projInfoString);
      break;
    case Projection.MERCATOR:
      tempProjection = new Mercator();
      tempProjection.setProjectionParametersFromString(projInfoString);
      break;
    case Projection.MILLER_CYLINDRICAL:
      tempProjection = new MillerCylindrical();
      tempProjection.setProjectionParametersFromString(projInfoString);
      break;
    case Projection.MOLLWEIDE:
      tempProjection = new Mollweide();
      tempProjection.setProjectionParametersFromString(projInfoString);
      break;
    case Projection.OBLATED_EQUAL_AREA:
      tempProjection = new OblatedEqualArea();
      tempProjection.setProjectionParametersFromString(projInfoString);
      break;
    case Projection.ORTHOGRAPHIC:
      tempProjection = new Orthographic();
      tempProjection.setProjectionParametersFromString(projInfoString);
      break;
    case Projection.POLAR_STEREOGRAPHIC:
      tempProjection = new PolarStereographic();
      tempProjection.setProjectionParametersFromString(projInfoString);
      break;
    case Projection.POLYCONIC:
      tempProjection = new Polyconic();
      tempProjection.setProjectionParametersFromString(projInfoString);
      break;
    case Projection.ROBINSON:
      tempProjection = new Robinson();
      tempProjection.setProjectionParametersFromString(projInfoString);
      break;
    case Projection.SINUSOIDAL:
      // need to see more to figure out if we want the new or old...
      if (projTypeClassString.indexOf("New") < 0) {
        // we have the old style projection object
        tempProjection = new Sinusoidal();
        tempProjection.setProjectionParametersFromString(projInfoString);
      } else {
        // we have the new style projection object
        tempProjection = new NewSinusoidal();
        tempProjection.setProjectionParametersFromString(projInfoString);
      }
      break;
    case Projection.SPACE_OBLIQUE_MERCATOR:
      tempProjection = new SpaceObliqueMercator();
      tempProjection.setProjectionParametersFromString(projInfoString);
      break;
    case Projection.STATE_PLANE_COORDINATES:
      tempProjection = new StatePlaneCoordinates();
      tempProjection.setProjectionParametersFromString(projInfoString);
      break;
    case Projection.STEREOGRAPHIC:
      tempProjection = new Stereographic();
      tempProjection.setProjectionParametersFromString(projInfoString);
      break;
    case Projection.TRANSVERSE_MERCATOR:
      tempProjection = new TransverseMercator();
      tempProjection.setProjectionParametersFromString(projInfoString);
      break;
    case Projection.UTM_NORTHERN_HEMISPHERE:
      tempProjection = new UTMNorth();
      tempProjection.setProjectionParametersFromString(projInfoString);
      break;
    case Projection.VAN_DER_GRINTEN:
      tempProjection = new VanderGrinten();
      tempProjection.setProjectionParametersFromString(projInfoString);
      break;
    case Projection.WAGNER_VII:
      tempProjection = new WagnerVII();
      tempProjection.setProjectionParametersFromString(projInfoString);
      break;
    case Projection.WAGNER_IV:
      tempProjection = new WagnerIV();
      tempProjection.setProjectionParametersFromString(projInfoString);
      break;
      
    case Projection.OBLIQUE_MERCATOR:
    default:
      System.err.println("ERROR: projection type not supported: index = " + projTypeInt + "; class = " + projTypeClassString);
    }
    
    // ok, now record the raster specific information...
    tempProjection.setEllipsoid(ellipsoidIndex);
    tempProjection.setRasterSpaceI(rsI);
    tempProjection.setRasterSpaceJ(rsJ);
    tempProjection.setRasterSpaceK(rsK);
    tempProjection.setInsertionX(iX);
    tempProjection.setInsertionY(iY);
    tempProjection.setInsertionZ(iZ);
    tempProjection.setScaleX(sX);
    tempProjection.setScaleY(sY);
    tempProjection.setScaleZ(sZ);
    tempProjection.setNumRows(nRows);
    tempProjection.setNumCols(nCols);
    
    return tempProjection;
  }
  
}
