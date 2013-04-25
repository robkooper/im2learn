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
package edu.illinois.ncsa.isda.im2learn.core.io.shapefile;

import java.io.*;

import org.apache.commons.logging.*;

import edu.illinois.ncsa.isda.im2learn.core.io.util.*;

/**
 * A representation of the header in an ESRI shapefile
 * @author ssaha
 * @author pbajcsy
 * @author clutter
 * @version 2.0
 */
public class ShapefileHeader
    implements Serializable {
  //private final static boolean DEBUG=true; //false;
  private int fileCode = -1;
  private int fileLength = -1;
  private int indexLength = -1;
  private int version = -1;
  private int shapeType = -1;
  private double[] bounds = new double[4];
  static private Log logger = LogFactory.getLog(ShapefileLoader.class);

  public ShapefileHeader() {
  }

  /**
   * Read in a ShapefileHeader from an LEDataInputStream
   * @param file
   * @throws IOException
   */
  public ShapefileHeader(LEDataInputStream file) throws IOException {
    file.setLittleEndianMode(false);
    // read the type
    fileCode = file.readInt();
    logger.debug("Filecode " + fileCode);
    if (fileCode != ShapefileLoader.FILE_CODE) {
      logger.error("Sfh->WARNING filecode " + fileCode +
                         " not a match for documented shapefile code " +
                         ShapefileLoader.FILE_CODE);
      //throw new IOException("File ID in header not that of a Shapefile  (Found "+ fileCode+" : Expected "+Shapefile.SHAPEFILE_ID+")");
      //file.skipBytes(20);//Skip unused part of header
    }
    // read blank ints
    for (int i = 0; i < 5; i++) {
      int tmp = file.readInt();
      logger.debug("Blank " + tmp);
    }
    // read the file length
    fileLength = file.readInt();
    logger.debug("Shapefile FileLength: " + fileLength);
    file.setLittleEndianMode(true);
    // read the version
    version = file.readInt();
    // read the shapefile type
    shapeType = file.readInt();
    logger.debug("Shapefile Type: " + shapeType);

    //read in the global bounding box
    for (int i = 0; i < 4; i++) {
      bounds[i] = file.readDouble();
      //logger.debug("shapefile bounds : " + bounds[i]);
    }

    //skip remaining unused bytes

    file.setLittleEndianMode(false); //well they may not be unused forever...
    file.skipBytes(32); // from the 68th to the 100th byte.
  }

  /**
   * Create a ShapefileHeader with the specified data
   * @param shapeType
   * @param bbox
   * @param shapes
   */
  public ShapefileHeader(int shapeType, double[] bbox, ShapefileShape[] shapes) {
    logger.debug("ShapefileHeader constructed with type " + shapeType);
    this.shapeType = shapeType;
    version = ShapefileLoader.VERSION;
    fileCode = ShapefileLoader.FILE_CODE;
    bounds = bbox;
    fileLength = 0;
    for (int i = 0; i < shapes.length; i++) { // Calculating the length of the file.
      fileLength += shapes[i].getLength();
      fileLength += 4; //for each header
    }
    fileLength += 50; //space used by this, the main header
    indexLength = 50 + (4 * shapes.length);
  }

  public void setFileLength(int fileLength) {
    this.fileLength = fileLength;
  }

  public void setFileCode(int fileCode) {
    this.fileCode = fileCode;
  }

  public void setShapeType(int shapeType) {
    this.shapeType = shapeType;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public void setIndexLength(int indexLength) {
    this.indexLength = indexLength;
  }

  public void setBBox(double[] bbox) {
    bounds = bbox;
  }

  /**
   * Write the shapefile out
   * @param file
   * @throws IOException
   */
  public void write(LEDataOutputStream file) throws IOException {
    int pos = 0;
    file.setLittleEndianMode(false);
    // write the code
    file.writeInt(fileCode);
    pos += 4;
    // write out blank ints
    for (int i = 0; i < 5; i++) {
      file.writeInt(0); //Skip unused part of header
      pos += 4;
    }
    // write out the length
    file.writeInt(fileLength);
    pos += 4;
    file.setLittleEndianMode(true);
    // write out the version
    file.writeInt(version);
    pos += 4;
    // write out the shapefile type
    file.writeInt(shapeType);
    pos += 4;
    // write out the bounding box
    for (int i = 0; i < 4; i++) {
      pos += 8;
      file.writeDouble(bounds[i]);
    }

    //skip remaining unused bytes
    //file.setLittleEndianMode(false);//well they may not be unused forever...
    for (int i = 0; i < 4; i++) {
      file.writeDouble(0.0); //Skip unused part of header
      pos += 8;
    }
  }

  public void writeToIndex(LEDataOutputStream file) throws IOException {
    int pos = 0;
    file.setLittleEndianMode(false);
    file.writeInt(fileCode);
    pos += 4;
    for (int i = 0; i < 5; i++) {
      file.writeInt(0); //Skip unused part of header
      pos += 4;
    }
    file.writeInt(indexLength);
    pos += 4;
    file.setLittleEndianMode(true);
    file.writeInt(version);
    pos += 4;
    file.writeInt(shapeType);
    pos += 4;
    //write the bounding box
    for (int i = 0; i < 4; i++) {
      pos += 8;
      file.writeDouble(bounds[i]);
    }
    //skip remaining unused bytes
    //file.setLittleEndianMode(false);//well they may not be unused forever...
    for (int i = 0; i < 4; i++) {
      file.writeDouble(0.0); //Skip unused part of header
      pos += 8;
    }
  }

  public int getShapeType() {
    return shapeType;
  }

  public int getFileLength() {
    return fileLength;
  }

  public int getIndexLength() {
    return indexLength;
  }

  public int getVersion() {
    return version;
  }

  public double[] getBounds() {
    return bounds;
  }

  public String toString() {
    String res = new String("Sf-->type " + fileCode + " size " + fileLength +
                            " version " + version + " Shape Type " + shapeType);
    return res;
  }
}
