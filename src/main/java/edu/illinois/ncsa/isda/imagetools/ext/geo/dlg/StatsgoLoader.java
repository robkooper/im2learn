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
//package ncsa.i2k.io.dlg;
package edu.illinois.ncsa.isda.imagetools.ext.geo.dlg;

import java.io.*;

import javax.swing.table.DefaultTableModel;

import edu.illinois.ncsa.isda.imagetools.core.datatype.*;
import edu.illinois.ncsa.isda.imagetools.core.io.shapefile.*;


/**
 * A loader for STATSGO data.   The data can be in either DLG or Shapefile
 * format.  When in Shapefile format, the DBF data is not currently read.
 *
 * @author clutter
 * @todo read in the DBF data and put into a Table.
 */
public class StatsgoLoader {

/*  public static void main(String[] args) throws Exception {
    StatsgoLoader sl = new StatsgoLoader(
        "/home/clutter/projects/waterqual/statsgo/il/spatial/il.dlg");
    //ShapefileLoader sl = new ShapefileLoader("/home/clutter/datasets/IL_ShapeFiles/111302_il_county_crime.shp");

    ShapeObject so = sl.GetShapeObject();
  }*/


  /**
   * the shape object that contains boundary information
   */
  private ShapeObject _shapeObject;

  /**
   * The attribute table contains a record for each boundary
   */
  DefaultTableModel attributeTable;

  /**
   * the DLG3 object reads in DLG data
   */
  private DLG3Loader dlg;

  /**
   * The shapefile loader that reads in shapefile data
   */
  private ShapefileLoader shapefile;

  /**
   * Read Statsgo data.  If the file ends in .shp, the shapefile loader will
   * be used, otherwise the DLG loader will be used
   * @param dlgFile File the file containing data
   * @throws Exception
   */
  public StatsgoLoader(File dlgFile) throws Exception {
    if(dlgFile.getAbsolutePath().endsWith(".shp")) {
      shapefile = new ShapefileLoader(dlgFile);
    }
    else {
      dlg = new DLG3Loader(dlgFile);
    }
  }

  /**
   * Read Statsgo data.  If the file ends in .shp, the shapefile loader will
   * be used, otherwise the DLG loader will be used
   * @param dlgFile String the file containing data
   * @throws Exception
   */
  public StatsgoLoader(String dlgFile) throws Exception {
    this(new File(dlgFile));
  }

/*  public StatsgoLoader(String dlgFile, String attFile) throws Exception {
    dlg = new DLG3Loader(new File(dlgFile));

    // LAM -- need to do something with the attFile here..
    // could add the appropriate columns from attFile to the attributeTable
    // but then there would be lots of redundant data..
  }*/

  /**
   * Load in a STATSGO data set.  Currently only supports DLG format.
   * @param shapeFile
   * @param attributeFile
   * @throws Exception
   */
/*  public STATSGOSpatialLoader(String shapeFile, String attributeFile, String tabularDirectory) throws Exception {
    //dlg = new DLG3(new File(shapeFile), new File(attributeFile));
    dlg = new DLG3(new File(shapeFile));

    // read the attribute file manually
    try {
      BufferedReader br = new BufferedReader(new FileReader(attributeFile));
      String line = null;

      int numLines = 0;
      boolean firstTime = true;
      int numColumns;
      while( (line = br.readLine()) != null) {
        numLines++;
        if(firstTime) {
          String[] colNames = line.split(" ");
          numColumns = colNames.length;
          firstTime = false;
        }
      }

      attributeTable = new MutableTableImpl();
      attributeTable.addColumn(new StringColumn(numLines));
      attributeTable.addColumn(new StringColumn(numLines));

      br = new BufferedReader(new FileReader(attributeFile));
      int row = 0;
      while( (line = br.readLine()) != null) {
        String[] colNames = line.split(" ");
        for (int i = 0; i < colNames.length; i++) {
          attributeTable.setString(colNames[i], row, i);
        }
        row++;
      }

    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }*/

  /**
   * Get the shape object.
   * @return
   */
  public ShapeObject GetShapeObject() {

    // if it has not been constructed, construct it now.
    // copy all the relevant data from DLG3 object into the shape object
    if (_shapeObject == null) {
      if(dlg != null) {

        int numBoundaries = dlg.boundaryTypes.size();

        _shapeObject = new ShapeObject(numBoundaries,
                                       dlg.globalBoundingBox,
                                       dlg.boundaryTypes.toNativeArray(),
                                       dlg.boundingBoxes.toNativeArray(),
                                       dlg.numberOfPointsInBoundary.
                                       toNativeArray(),
                                       dlg.boundaryPoints.toNativeArray(),
                                       dlg.boundaryPointsIndex.toNativeArray(),
                                       dlg.numberOfPartsInEachBoundary.
                                       toNativeArray(),
                                       dlg.boundaryPartsIndex.toNativeArray(),
                                       null);

        double minLongitude = Double.POSITIVE_INFINITY;
        double maxLongitude = Double.NEGATIVE_INFINITY;
        double minLatitude = Double.POSITIVE_INFINITY;
        double maxLatitude = Double.NEGATIVE_INFINITY;

        // for each boundary, check the bounds
        for (int i = 0; i < numBoundaries; i++) {
          double[] bb = _shapeObject.GetBndBox(i);
          if (bb[0] < minLongitude) {
            minLongitude = bb[0];
          }
          if (bb[1] < minLatitude) {
            minLatitude = bb[1];
          }
          if (bb[2] > maxLongitude) {
            maxLongitude = bb[2];
          }
          if (bb[3] > maxLatitude) {
            maxLatitude = bb[3];
          }
        }

        _shapeObject.setGlobalBoundingBox(new double[] {
                                          minLongitude, minLatitude,
                                          maxLongitude, maxLatitude});
        //_shapeObject._globalBndBox = new double[] {yMin, xMin, yMax, xMax};
        /*      for (int i = 0; i < 4; i++) {
         System.out.println("bounds[" + i + "]: " + _shapeObject._globalBndBox[i]);
              }*/

        // for each boundary
        /*int numBound = _shapeObject.GetNumBnd();
               for(int i = 0; i < numBound; i++) {
          int numParts = _shapeObject.GetNumBndParts(i);
          for(int j = 0; j < numParts; j++) {
            try {
              _shapeObject.GetPartPointsForBnd(i, j);
            }
            catch(Exception e) {
         System.out.println("boundary: "+i+" numParts: "+numParts+" partNum: "+j);
              int[] parts = _shapeObject.GetPartsForBnd(i);
              for(int q = 0; q < parts.length; q++) {
                System.out.println("part index: "+parts[q]);
              }
              return null;
            }
          }
               }*/
        // for each boundary, get the bounding box.
        //bounds = _shapeObject.GetGlobalBndBox();

        attributeTable = new DefaultTableModel();
        attributeTable.addColumn("MAJOR");
        attributeTable.addColumn("MINOR");

        for (int i = 0; i < numBoundaries; i++) {
			int maj = dlg.majorCodes.get(i);
			int min = dlg.minorCodes.get(i);

			attributeTable.addRow(new Integer[]{maj, min});
		}

        /*      StringColumn sc = new StringColumn(attributeCodes.size());
              Iterator iter = attributeCodes.iterator();
              int idx = 0;
              int numBlank = 0;
              while (iter.hasNext()) {
                String v = (String) iter.next();
                //if(v.trim().equals("BLANK"))
                //  numBlank++;
                sc.setString(v, idx);
                idx++;
              }
              attributeTable = new MutableTableImpl(new Column[] {sc});
         */
//        attributeTable = new MutableTableImpl(new Column[] {major, minor});
      }
      else if(shapefile != null) {
        _shapeObject = shapefile.getShapeObject();

        // get the attribute table..
      }

    }
    return _shapeObject;
  }

	public DefaultTableModel GetDBFTable() {
		if(attributeTable == null)
		  GetShapeObject();
		return attributeTable;
	}
}
