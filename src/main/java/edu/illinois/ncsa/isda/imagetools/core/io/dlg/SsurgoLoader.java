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
package edu.illinois.ncsa.isda.imagetools.core.io.dlg;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import javax.swing.table.DefaultTableModel;

import edu.illinois.ncsa.isda.imagetools.core.datatype.*;

import gnu.trove.*;

/**
 * Load a SSURGO soil DB.
 * @author clutter
 * @TODO right now only loads dlg files, and they must be in a zip named dlg.zip
 */
public class SsurgoLoader {

//  public static void main(String[] args) throws Exception {
    /*long start = System.currentTimeMillis();
    //SSURGOLoader sl = new SSURGOLoader("/home/clutter/datasets/il19_2002/");
    //ShapeObject so = sl.GetShapeObject();
    Shapefile s = new Shapefile("/home/clutter/datasets/il19_2002/shp/il119_a.shp");
    ShapeObject so = s.GetShapeObject();
    long end = System.currentTimeMillis();
    long total = Runtime.getRuntime().totalMemory();
    long free = Runtime.getRuntime().freeMemory();
    System.out.println("Num Millis: "+(end-start));
    System.out.println("TOTAL: "+total);
    System.out.println("USED: "+(total-free));
*/
/*    long start = System.currentTimeMillis();
    ShapefileLoader sl = new ShapefileLoader("/home/clutter/datasets/il19_2002/shp/il119_a.shp");
    //SSURGOSpatialLoader sl = new SSURGOSpatialLoader("/home/clutter/datasets/il19_2002/");
//    SSURGOSpatialLoader sl = new SSURGOSpatialLoader("/home/clutter/datasets/il19_2002/dlg",
//        "/home/clutter/datasets/il19_2002/tab");
    ShapeObject so = sl.GetShapeObject();

/*    Shapefile s1 = new Shapefile("/home/clutter/Census/shapefiles/17_IL_final_BG.shp");
    ShapeObject so1 = s1.GetShapeObject();
    Shapefile s2 = new Shapefile("/home/clutter/Census/shapefiles/17_IL_final_County.shp");
    ShapeObject so2 = s2.GetShapeObject();
    Shapefile s3 = new Shapefile("/home/clutter/Census/shapefiles/17_IL_final_Tract.shp");
    ShapeObject so3 = s3.GetShapeObject();
    Shapefile s4 = new Shapefile("/home/clutter/Census/shapefiles/17_IL_final_ZCTA.shp");
    ShapeObject so4 = s4.GetShapeObject();
 */

/*    long end = System.currentTimeMillis();
    System.out.println("Num Millis: "+(end-start));
//    System.out.println("Num Bnd: "+(so1.GetNumBnd()+so2.GetNumBnd()+so3.GetNumBnd()+so4.GetNumBnd()));

/*    Shapefile shp = new Shapefile("/home/clutter/datasets/Test/IL_County.shp");
    ShapeObject shape = shp.GetShapeObject();*/
/*ShapeObject shape = so;
    int numPoints = 0;
    HashSet pointMap = new HashSet();

    // count up the number of points.

    // for each boundary, put its points in a hashmap
    int numBoundaries = shape.GetNumBnd();
    for(int i = 0; i < numBoundaries; i++) {
      int numParts = shape.GetNumBndParts(i);
      for(int j = 0; j < numParts; j++) {
        double[] partPoints = shape.GetPartPointsForBnd(i, j);
        numPoints += partPoints.length/2;
        for(int z = 0; z < partPoints.length; z+=2) {
          java.awt.geom.Point2D.Double p2d = new java.awt.geom.Point2D.Double(partPoints[z], partPoints[z+1]);
          pointMap.add(p2d);
        }
      }
    }

    System.out.println("NumPoints: "+numPoints);
    System.out.println("Map size: "+pointMap.size());
  }*/

  /** file name extensions */
  private static final String AA = "aa";
  private static final String SA = "sa";
  private static final String AF = "af";
  private static final String SF = "sf";

  /** this is a list of all the quadrangles (in DLG format) that we have processed
   * so far. At the end, they will be merged into one big ShapeObject
   */
  private List dlgList;
  /** the shape object that contains all the boundaries, etc */
  private ShapeObject _shapeObject;
  /** the attributes */
  private DefaultTableModel _attributeTable;

  /**
   *
   * @param dir
   */
  public SsurgoLoader(String dir) {
    File directory = new File(dir);
    dlgList = new ArrayList();

    if (directory.isDirectory()) {
      examineDirectory(directory);
    }
    //GetShapeObject();
  }

  public SsurgoLoader(String dlgDir, String tabDir) {
    dlgList = new ArrayList();
    try {
      processDLGData(new File(dlgDir), new File(tabDir));
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  public SsurgoLoader(String dlgZip, String tabZip, boolean flag) {
    dlgList = new ArrayList();
    try {
      processZipDLGData(new File(dlgZip), new File(tabZip));
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void examineDirectory(File directory) {
    // examine all files in this directory and any subdirectories
    File[] files = directory.listFiles();

    // for each file..

    // process dlg.zip + tab.zip

    // process shp.zip? + tab.zip

    // recurse into subdirectories

    File dlgFile = null;
    File tabFile = null;
    File shpFile = null;

    int numFiles = files.length;
    for (int i = 0; i < numFiles; i++) {
      if (files[i].getName().equalsIgnoreCase("dlg.zip")) {
        dlgFile = files[i];
      }
      else if (files[i].getName().equalsIgnoreCase("tab.zip")) {
        tabFile = files[i];
      }
      else if (files[i].getName().equalsIgnoreCase("shpFile")) {
        shpFile = files[i];
      }
    }

    if (dlgFile != null && tabFile != null) {
      //System.out.println("DLG: " + dlgFile.getAbsolutePath() + " TAB: " +
      //                   tabFile.getAbsolutePath());
      // process
      try {
        processZipDLGData(dlgFile, tabFile);
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
    /*else if (shpFile != null && tabFile != null) {
      System.out.println("SHP: " + shpFile.getAbsolutePath() + " TAB: " +
                         tabFile.getAbsolutePath());
      // process
    }*/

    for (int i = 0; i < numFiles; i++) {
      if (files[i].isDirectory()) {
        examineDirectory(files[i]);
      }
    }
  } // examine directory

  private void processDLGData(File spDir, File tabularDir) throws Exception {
        File[] files = spDir.listFiles();

        // keep a set of the files, pluck out files when they are used
        HashSet set = new HashSet();
        int num = files.length;
        for (int i = 0; i < num; i++) {
          set.add(files[i]);
        }

        // examine each file
        // ignore if it is not in the set anymore (this means it has already been processed)
        for (int i = 0; i < num; i++) {
          File f = files[i];
          if (!set.contains(f)) {
            continue;
          }

          String name = f.getName();

          // if it ends with "af", this is a soil polygon DLG-3 file
          if (name.endsWith(AF)) {
            // find the soil polygon attribute file
            String pth = f.getAbsolutePath();
            pth = pth.substring(0, pth.length() - 2);
            pth += AA;

            File attributeFile = new File(pth);
            if (!set.contains(attributeFile)) {
              System.out.println("Couldn't find attribute file: " +
                                 attributeFile.getAbsolutePath());
              continue;
            }

            set.remove(f);
            set.remove(attributeFile);

            readSoilPolygonFile(f, attributeFile);
          }

          // if it ends with "aa", this is a soil polygon attribute file
          else if (name.endsWith(AA)) {
            // find the soil polygon DLG-3 file
            String pth = f.getAbsolutePath();
            pth = pth.substring(0, pth.length() - 2);
            pth += AF;

            File polygonFile = new File(pth);
            if (!set.contains(polygonFile)) {
              System.out.println("Couldn't find polygon file: " +
                                 polygonFile.getAbsolutePath());
              continue;
            }

            set.remove(f);
            set.remove(polygonFile);

            readSoilPolygonFile(polygonFile, f);
          }

          // if it ends with "sf", this is a special soil polygon DLG-3 file
          else if (name.endsWith(SF)) {
            // find the soil polygon attribute file
            String pth = f.getAbsolutePath();
            pth = pth.substring(0, pth.length() - 2);
            pth += SA;

            File attributeFile = new File(pth);
            if (!set.contains(attributeFile)) {
              System.out.println("Couldn't find attribute file: " +
                                 attributeFile.getAbsolutePath());
              continue;
            }

            set.remove(f);
            set.remove(attributeFile);

            //readSoilPolygonFile(f, attributeFile);
          }

          // if it ends with "sa", this is a special soil polygon attribute file
          else if (name.endsWith(SA)) {
            // find the soil polygon DLG-3 file
            String pth = f.getAbsolutePath();
            pth = pth.substring(0, pth.length() - 2);
            pth += SF;

            File polygonFile = new File(pth);
            if (!set.contains(polygonFile)) {
              System.out.println("Couldn't find polygon file: " +
                                 polygonFile.getAbsolutePath());
              continue;
            }

            set.remove(f);
            set.remove(polygonFile);

            //readSoilPolygonFile(polygonFile, f);
          }
          /*else if (name.equals("mapunit.txt")) {
            mapunit = readMapunitTable(f);
          }
          else if(name.equals("feature")) {
            feature = readFeatureTable(f);
          }
          else if (f.isDirectory()) {
            examineDirectory(f);
          }*/
        }

        //System.out.println("End: "+set);
  }

  private void processZipDLGData(File dlg, File tab) throws Exception {
    ZipFile dlgZip = new ZipFile(dlg);

    ZipFile tabZip = new ZipFile(tab);
    // get the mapunit file..
    /*ZipEntry e = tabZip.getEntry("mapunit");
         if (e == null) {
      e = tabZip.getEntry("mapunit.txt");
         }
         if (e == null) {
      e = tabZip.getEntry("tab/mapunit");
         }
         if (e == null) {
      e = tabZip.getEntry("tab/mapunit.txt");
         }
         Table mapunitTable = null;
         if (e != null) {
      // read mapunit
      mapunitTable = readMapunitTable(tabZip.getInputStream(e));
         }
         else {
      mapunitTable = new MutableTableImpl();
         }*/

    // keep a set of the files, pluck out files when they are used
    HashSet set = new HashSet();
    Enumeration enumzip = dlgZip.entries();
    while (enumzip.hasMoreElements()) {
      ZipEntry entry = (ZipEntry) enumzip.nextElement();
      //if(entry.getName().indexOf("s3808909.1") != -1)
      set.add(entry.getName());
    }

    HashSet cpy = new HashSet(set);

    // examine each file
    // ignore if it is not in the set anymore (this means it has already been processed)
    Iterator iter = set.iterator();
    while (iter.hasNext()) {
      String name = (String) iter.next();
      if (!cpy.contains(name)) {
        continue;
      }

      // if it ends with "af", this is a soil polygon DLG-3 file
      if (name.endsWith(AF)) {
        // find the soil polygon attribute file
        //String pth = f.getAbsolutePath();
        String pth = name.substring(0, name.length() - 2);
        pth += AA;

        //File attributeFile = new File(pth);
        String attributeFile = pth;
        if (!set.contains(attributeFile)) {
          System.out.println("Couldn't find attribute file: " +
                             pth);
          continue;
        }

        cpy.remove(name);
        cpy.remove(attributeFile);
        readSoilPolygonFile(dlgZip.getEntry(name),
                            dlgZip.getEntry(attributeFile), dlgZip);
        //mapunitTable);
      }

      // if it ends with "aa", this is a soil polygon attribute file
      else if (name.endsWith(AA)) {
        // find the soil polygon DLG-3 file
        //String pth = f.getAbsolutePath();
        String pth = name.substring(0, name.length() - 2);
        pth += AF;

        //File polygonFile = new File(pth);
        String polygonFile = pth;
        if (!cpy.contains(polygonFile)) {
          System.out.println("Couldn't find polygon file: " +
                             pth);
          continue;
        }

        cpy.remove(name);
        cpy.remove(polygonFile);
        readSoilPolygonFile(dlgZip.getEntry(polygonFile), dlgZip.getEntry(name),
                            dlgZip); //, mapunitTable);
      }

      // if it ends with "sf", this is a special soil polygon DLG-3 file
      else if (name.endsWith(SF)) {
        // find the soil polygon attribute file
        //String pth = f.getAbsolutePath();
        String pth = name.substring(0, name.length() - 2);
        pth += SA;

        //File attributeFile = new File(pth);
        String attributeFile = pth;
        if (!cpy.contains(attributeFile)) {
          System.out.println("Couldn't find attribute file: " +
                             pth);
          continue;
        }

        cpy.remove(name);
        cpy.remove(attributeFile);
        readSoilPolygonFile(dlgZip.getEntry(name),
                            dlgZip.getEntry(attributeFile), dlgZip);
        //mapunitTable);
      }

      // if it ends with "sa", this is a special soil polygon attribute file
      else if (name.endsWith(SA)) {
        // find the soil polygon DLG-3 file
        //String pth = f.getAbsolutePath();
        String pth = name.substring(0, name.length() - 2);
        pth += SF;

        //File polygonFile = new File(pth);
        String polygonFile = pth;
        if (!set.contains(polygonFile)) {
          System.out.println("Couldn't find polygon file: " +
                             pth);
          continue;
        }

        cpy.remove(name);
        cpy.remove(polygonFile);

        readSoilPolygonFile(dlgZip.getEntry(polygonFile), dlgZip.getEntry(name),
                            dlgZip); //, mapunitTable);
      }
    }
  } // processDLG

  private void readSoilPolygonFile(ZipEntry af, ZipEntry aa, ZipFile zf) {
    //Table mapunit) {
    try {
      //DLG3 dlg = new DLG3(af, aa, zf);
      // LAM --- this needs to change to use aa!!
      DLG3Loader dlg = new DLG3Loader(af, zf);
      dlgList.add(dlg);
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private void readSoilPolygonFile(File af, File aa) {
    try {
      // LAM --- need to do something with aa here
      //DLG3 dlg = new DLG3(af, aa);
      DLG3Loader dlg = new DLG3Loader(af);
      dlgList.add(dlg);
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }

  public ShapeObject GetShapeObject() {
    if (_shapeObject == null) {

      // we have the DLG list.
      Object[] dlgs = dlgList.toArray();
      dlgList.clear();
      dlgList = null;
      int len = dlgs.length;

      int lenBoundaries = 0, curBoundary = 0;
      int lenBoundingBoxes = 0, curBoundingBox = 0;
      int lenPointsInBoundary = 0, curPointsInBoundary = 0;
      int lenBoundaryPoints = 0, curBoundaryPoints = 0;
      int lenBoundaryPointsIndex = 0, curBoundaryPointsIndex = 0;
      int lenPartsInEachBoundary = 0, curPartsInEachBoundary = 0;
      int lenBoundaryPartsIndex = 0, curBoundaryPartsIndex = 0;
      int curMajorCodesIndex = 0;
      int curMinorCodesIndex = 0;
      int curAttributeCodesIndex = 0;

      // iterate through once to find all the sizes of the arrays for the ShapeObj
      for (int i = 0; i < len; i++) {
        DLG3Loader dlg = (DLG3Loader) dlgs[i];

        lenBoundaries += dlg.boundaryTypes.size();
        lenBoundingBoxes += dlg.boundingBoxes.size();
        lenPointsInBoundary += dlg.numberOfPointsInBoundary.size();
        lenBoundaryPoints += dlg.boundaryPoints.size();
        lenBoundaryPointsIndex += dlg.boundaryPointsIndex.size();
        lenPartsInEachBoundary += dlg.numberOfPartsInEachBoundary.size();
        lenBoundaryPartsIndex += dlg.boundaryPartsIndex.size();
      }

      // allocate arrays
      int[] boundaryTypes = new int[lenBoundaries];
      double[] boundingBoxes = new double[lenBoundingBoxes];
      int[] numberOfPointsInBoundary = new int[lenPointsInBoundary];
      Point2DDouble p2d = new Point2DDouble(lenBoundaryPoints);
      int[] boundaryPointsIndex = new int[lenBoundaryPointsIndex];
      int[] numberOfPartsInEachBoundary = new int[lenPartsInEachBoundary];
      int[] boundaryPartsIndex = new int[lenBoundaryPartsIndex];

      int[] majorCodes = new int[lenBoundaries];
      int[] minorCodes = new int[lenBoundaries];
      //String[] attributeCodes = new String[lenBoundaries];

      // iterate through again and fill in arrays, null it once finished
      for (int i = 0; i < len; i++) {
        DLG3Loader dlg = (DLG3Loader) dlgs[i];
        // now we need to copy the data from dlg into the proper array.

        // boundaryTypes
        curBoundary = copyInto(dlg.boundaryTypes, boundaryTypes, curBoundary);

        // boundingBoxes
        curBoundingBox = copyInto(dlg.boundingBoxes, boundingBoxes,
                                  curBoundingBox);

        // numberOfPointsInBoundary
        curPointsInBoundary = copyInto(dlg.numberOfPointsInBoundary,
                                       numberOfPointsInBoundary,
                                       curPointsInBoundary);

        int size = curBoundaryPoints+1;
        // boundary points
        curBoundaryPoints = copyInto(dlg.boundaryPoints, p2d.ptsDouble,
                                     curBoundaryPoints);

        int num = dlg.boundaryPointsIndex.size();
        for(int j = 0; j < num; j++) {
          int val = dlg.boundaryPointsIndex.get(j);
          dlg.boundaryPointsIndex.set(j, val+size);
        }

        // boundaryPointsIndex
        curBoundaryPointsIndex = copyInto(dlg.boundaryPointsIndex,
                                          boundaryPointsIndex,
                                          curBoundaryPointsIndex);

        // numberOfPartsInEachBoundary
        curPartsInEachBoundary = copyInto(dlg.numberOfPartsInEachBoundary,
                                          numberOfPartsInEachBoundary,
                                          curPartsInEachBoundary);

        // boundaryPartsIndex
        curBoundaryPartsIndex = copyInto(dlg.boundaryPartsIndex,
                                         boundaryPartsIndex,
                                         curBoundaryPartsIndex);

        curMajorCodesIndex = copyInto(dlg.majorCodes,
                                      majorCodes,
                                      curMajorCodesIndex);
        curMinorCodesIndex = copyInto(dlg.minorCodes,
                                      minorCodes,
                                      curMinorCodesIndex);

        //curAttributeCodesIndex = copyInto(dlg.attributeCodes, attributeCodes,
        //                                  curAttributeCodesIndex);

        dlgs[i] = null;
      }

//      _shapeObject = new ShapeObject(1);
      int numBoundaries = boundaryTypes.length;
/*      _shapeObject._bndType = boundaryTypes;
      _shapeObject._bndBox = boundingBoxes;
      _shapeObject._numBndPts = numberOfPointsInBoundary;
      _shapeObject._bndPts = p2d;
      _shapeObject._bndPtsIdx = boundaryPointsIndex;
      _shapeObject._numBndParts = numberOfPartsInEachBoundary;
      _shapeObject._bndPartsIdx = boundaryPartsIndex;
      _shapeObject.SetNumBnd(numBoundaries);*/

      _shapeObject = new ShapeObject(numBoundaries, null,
                                     boundaryTypes, boundingBoxes,
                                     numberOfPointsInBoundary, p2d.ptsDouble,
                                     boundaryPointsIndex,
                                     numberOfPartsInEachBoundary,
                                     boundaryPartsIndex,
                                     null);

      // now, make the attribute table
      //this._attributeTable = new MutableTableImpl();
      _attributeTable = new DefaultTableModel();
      _attributeTable.addColumn("MAJOR");
      _attributeTable.addColumn("MINOR");

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

      //_shapeObject._globalBndBox = new double[] {
      //    minLongitude, minLatitude, maxLongitude, maxLatitude};
      _shapeObject.setGlobalBoundingBox(new double[] {
          minLongitude, minLatitude, maxLongitude, maxLatitude});
/*          System.out.println("***** boundary ******");
            for(int j = 0; j < 4; j++) {
              System.out.println(_shapeObject._globalBndBox[j]);
            }
            System.out.println();
            System.out.println();*/
    }

    return _shapeObject;
  }

  public DefaultTableModel GetDBFTable() {
    return _attributeTable;
  }

  private static int copyInto(TIntArrayList src, int[] dest, int destStart) {
    int index = destStart;
    int size = src.size();
    for (int i = 0; i < size; i++) {
      dest[index] = src.get(i);
      index++;
    }
    return index;
  }

  private static int copyInto(TDoubleArrayList src, double[] dest,
                              int destStart) {
    int index = destStart;
    int size = src.size();
    for (int i = 0; i < size; i++) {
      dest[index] = src.get(i);
      index++;
    }
    return index;
  }

  private static int copyInto(ArrayList src, String[] dest,
                              int destStart) {
    int index = destStart;
    int size = src.size();
    for (int i = 0; i < size; i++) {
      dest[index] = (String)src.get(i);
      index++;
    }
    return index;
  }

}
