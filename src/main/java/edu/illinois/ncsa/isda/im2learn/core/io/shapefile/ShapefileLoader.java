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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.datatype.Point2DDouble;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ShapeObject;
import edu.illinois.ncsa.isda.im2learn.core.geo.Datum;
import edu.illinois.ncsa.isda.im2learn.core.geo.GeoException;
import edu.illinois.ncsa.isda.im2learn.core.geo.GeoGraphicCoordinateSystem;
import edu.illinois.ncsa.isda.im2learn.core.geo.PRJLoader;
import edu.illinois.ncsa.isda.im2learn.core.geo.Projection;
import edu.illinois.ncsa.isda.im2learn.core.geo.Projection.ProjectionType;
import edu.illinois.ncsa.isda.im2learn.core.io.DBFLoader;
import edu.illinois.ncsa.isda.im2learn.core.io.util.LEDataInputStream;
import edu.illinois.ncsa.isda.im2learn.core.io.util.LEDataOutputStream;

/**
 * An API to read and write shapefiles. The records of a shapefile are kept as objects in an ArrayList. The shapefile
 * data is accessible via a ShapeObject. Use the getShapeObject() method.
 * 
 * @TODO merging shapefile code is currently commented out
 * @TODO DBF reading and writing needs to be standardized! what if readDBF was in this class, but had to be called
 *       manually?
 * 
 * @author ssaha
 * @author pbajcsy
 * @author clutter
 * @version 2.0
 */
public class ShapefileLoader {

	/** the logger */
	static private Log			logger					= LogFactory.getLog(ShapefileLoader.class);

	// Some constants
	// private final static boolean DEBUG=false;
	public static final int		FILE_CODE				= 9994;
	public static final int		VERSION					= 1000;

	private static final String	SHP_EXTENSION			= ".shp";
	private static final String	SHX_EXTENSION			= ".shx";
	private static final String	DBF_EXTENSION			= ".dbf";
	private static final String	PRJ_EXTENSION			= ".prj";

	public static final int		NULL					= 0;
	public static final int		POINT					= 1;
	public static final int		ARC						= 3;
	public static final int		POLYGON					= 5;
	public static final int		MULTIPOINT				= 8;
	public static final int		POLY_LINE_Z				= 13;
	public static final int		ARC_M					= 23;
	public static final int		UNDEFINED				= -1;
	// Types 2,4,6,7 and 9 were undefined at time of writing

	private final int			_mergeDelta				= 5;										// DEFAULT
	private ShapefileHeader		_mainHeader				= null;
	// this can now be populated by any method that has information
	// about its member variables
	private ArrayList			_records;
	private double[]			_globalBndBox;

	private ShapeObject			_shapeObject			= null;
	private final int			_mergedRecordsLength	= 0;
	private final double[]		_mergedGlobalBBox		= new double[4];
	private final boolean		_firstGBoxValue			= true;
	private int					MERGED_FILE_SHAPETYPE;

	/**
	 * The name of the .shp file (can also be a URL).
	 */
	private String				_shpFileName;

	/**
	 * empty constructor
	 */
	public ShapefileLoader() {
		_records = null;
		_shapeObject = null;
	}

	/**
	 * Creates and initializes a shapefile from disk. Handles cases when filename has a 3-char suffix ('.shp', '.shx',
	 * '.dbf') or no suffix at all.
	 * 
	 * @param filename
	 *            (including path) of the shapefile.
	 */
	public ShapefileLoader(String filename) throws java.io.IOException, ShapefileException {

		// figure out the dot.
		String fourthChar = filename.substring(filename.length() - 4, filename.length() - 3);

		// if we have the dot, delete the extension.
		if (fourthChar.equals(".")) {
			filename = filename.substring(0, filename.length() - 4);
		}

		// at this point, we have the prefix. concatenate it with SHP suffix.
		this._shpFileName = filename.concat(SHP_EXTENSION);

		// create the data input stream from the file.
		LEDataInputStream sfile = new LEDataInputStream(new FileInputStream(this._shpFileName));

		// read it in
		init(sfile);
	}

	/**
	 * Creates and initialises a shapefile from a url
	 * 
	 * @param url
	 *            The url of the shapefile
	 */
	public ShapefileLoader(URL url) throws java.io.IOException, ShapefileException {
		// get a connection
		URLConnection uc = url.openConnection();

		// check the size of the file
		int len = uc.getContentLength();
		if (len <= 0) {
			throw new IOException("Sf-->File feched from URL " + url + " was of zero length or could not be found");
		}

		// set up the name of the file (in part, for use by PRJ loader).
		this._shpFileName = url.toString();

		// get the data
		byte data[];
		data = new byte[len];
		BufferedInputStream in = new BufferedInputStream(uc.getInputStream());

		// read in all the data
		int j = 0, k = 0;
		while ((k < len) || (j == -1)) {
			j = in.read(data, k, len - k);
			k += j;
		}

		// create the data input stream from data.
		LEDataInputStream sfile = new LEDataInputStream(new ByteArrayInputStream(data));

		// read it in
		init(sfile);
	}

	/**
	 * Creates and initialised a shapefile from disk
	 * 
	 * @param file
	 *            a File that represents the shapefile
	 */
	public ShapefileLoader(File file) throws java.io.IOException, ShapefileException {

		// set up the name of the file (in part, for use by PRJ loader).
		this._shpFileName = file.getAbsolutePath();

		// create the data input stream
		LEDataInputStream sfile = new LEDataInputStream(new FileInputStream(file));

		// read it in
		init(sfile);
	}

	/**
	 * Creates and initalises a shapefile from an inputstream
	 * 
	 * @param inputstream
	 *            with the shapefile is at the other end
	 */
	public ShapefileLoader(InputStream in) throws java.io.IOException, ShapefileException {

		// create the data input stream
		LEDataInputStream sfile = new LEDataInputStream(in);
		// read it in
		init(sfile);
	}

	/*  public Shapefile(int shapeType, double[] bbox, ShapefileShape[] shapes) {
	    _mainHeader = new ShapefileHeader(shapeType, bbox, shapes);
	    _records = new Vector(shapes.length);
	    for (int i = 0; i < shapes.length; i++) {
	      _records.addElement(new ShapeRecord(i + 1, shapes[i])); //Sunayana: _records used here for uniformity.
	    }
	  }*/

	// LAM --- THIS IS THE SAME AS ONE OF THE CONSTRUCTORS. WHY HAVE BOTH?
	/**
	 * Initialises a shapefile from disk. Use Shapefile(String) if you don't want to use LEDataInputStream directly
	 * (recommended)
	 * 
	 * @param file
	 *            A LEDataInputStream that conects to the shapefile to read
	 */
	/*  public boolean read(String filename) throws java.io.IOException,
	      ShapefileException {
	    // The filename should not have the '.shp' extn.
	    FileInputStream in = null;
	    /*if (filename == null) {
	        System.err.println("Filename null in Read for shapefiles");
	        return false;
	           }*/
	/*    try {
	      // check the extension
	     String suf = filename.substring(filename.length() - 4, filename.length());
	      // if it is .shp, it is ok
	      if (suf.equalsIgnoreCase(SHP_EXTENSION)) {
	        in = new FileInputStream(filename);
	      }
	      // if it is .shx, use the .shp file instead
	      else {
	        if (suf.equalsIgnoreCase(SHX_EXTENSION)) {
	          filename = filename.substring(0, filename.length() - 4);
	        }
	        in = new FileInputStream(filename.concat(SHP_EXTENSION));
	      }
	    }
	    catch (Exception exp) {
	      System.err.println("No Input File to read: " + filename);
	      return false;
	    }
	    // create the data input stream
	    LEDataInputStream sfile = new LEDataInputStream(in);
	    // read it
	    return (init(sfile));
	  }*/

	/**
	 * Read in all the records of the shapefile. The records are kept in an ArrayList.
	 * 
	 * @param file
	 * @throws IOException
	 * @throws ShapefileException
	 */
	private synchronized void init(LEDataInputStream file) throws IOException, ShapefileException {
		_shapeObject = null;
		_mainHeader = new ShapefileHeader(file);
		_globalBndBox = _mainHeader.getBounds();

		int indexLength = 50;
		int numRecords = 0;
		if (_mainHeader.getVersion() < VERSION) {
			logger.warn("Warning: Shapefile format (" + _mainHeader.getVersion() + ") older that supported (" + VERSION
					+ "), attempting to read anyway");
		}
		if (_mainHeader.getVersion() > VERSION) {
			logger.warn("Warning: Shapefile format (" + _mainHeader.getVersion() + ") newer that supported (" + VERSION
					+ "), attempting to read anyway");
		}
		_records = new ArrayList();
		ShapefileShape body;
		RecordHeader header;
		int type = _mainHeader.getShapeType();
		try {
			for (;;) {
				header = new RecordHeader(file);
				numRecords++; // This is needed to set the indexLength for the index file
				switch (type) {
				case (POINT):
					body = new ShapePoint(file);
					break;
				case (ARC):
					body = new ShapeArc(file);
					break;
				case (POLYGON):
					body = new ShapePolygon(file);
					break;
				case (ARC_M):
					body = new ShapeArcM(file);
					break;
				case (ShapefileLoader.POLY_LINE_Z):
					body = new ShapePolyLineZ(file);
					break;

				default:
					throw new ShapeTypeNotSupportedException("Shape type " + getShapeTypeDescription() + " [" + type
							+ "] not suported");
				}
				_records.add(new ShapeRecord(header, body));
			}
		} catch (EOFException e) {
			logger.debug("Finished reading " + _records.size() + " shapes now at EOF");
			indexLength += 4 * numRecords;
			_mainHeader.setIndexLength(indexLength); // SET THIS FOR INDEX FILE
		}
	}

	/**
	 * Saves a shapefile to a file defined by a file name
	 */
	public void Write(String OutFileName) throws IOException {
		// The OutFileName should not have the '.shp' extn.

		// get the suffix
		String suf = OutFileName.substring(OutFileName.length() - 4, OutFileName.length());
		String OutFileName1 = null;
		
		// if the file ends with .shx or .dbf, strip the suffix and
		// append .shp
		OutFileName1 = new String(OutFileName);
		if ((suf.equalsIgnoreCase(SHX_EXTENSION)) || (suf.equalsIgnoreCase(DBF_EXTENSION))
				|| (suf.equalsIgnoreCase(PRJ_EXTENSION))) {
			OutFileName1 = OutFileName.substring(0, OutFileName.length() - 4);
			OutFileName1 = OutFileName.concat(SHP_EXTENSION);
		}
		// otherwise if the suffix is not .shp, make it .shp
		else if (!suf.equalsIgnoreCase(SHP_EXTENSION)) {
			OutFileName1 = OutFileName.concat(SHP_EXTENSION);
		}

		// create the file output stream
		FileOutputStream os = new FileOutputStream(OutFileName1);

		// if the records are null, we can't go on.
		if (_records == null) {
			// sanity check
			logger.error("Records null, cannot write shapefile ");
			throw new IOException("Records null, cannot write shapefile.");
		}

		// write out the .shp
		try {
			writeShapefile(os);
		} catch (IOException ex) {
			logger.error("Could not write '.shp' file", ex);
		}

		// if file ends with .shp or .dbf, strip the suffix and
		// append .shx
		OutFileName1 = new String(OutFileName);
		if ((suf.equalsIgnoreCase(SHP_EXTENSION)) || (suf.equalsIgnoreCase(DBF_EXTENSION))
				|| (suf.equalsIgnoreCase(PRJ_EXTENSION))) {
			OutFileName1 = OutFileName.substring(0, OutFileName.length() - 4);
			OutFileName1 = OutFileName.concat(SHX_EXTENSION);
		}
		// otherwise if the suffix is not .shx, make it .shx
		else if (!suf.equalsIgnoreCase(SHX_EXTENSION)) {
			OutFileName1 = OutFileName.concat(SHX_EXTENSION);
		}

		// write out the .shx
		try {
			WriteIndexFile(OutFileName1);
		} catch (IOException ex) {
			logger.error("Could not write '.shx' file", ex);
		}

		// if file ends with .shp or .shx, strip the suffix and
		// append .dbf
		OutFileName1 = new String(OutFileName);
		if ((suf.equalsIgnoreCase(SHP_EXTENSION)) || (suf.equalsIgnoreCase(SHX_EXTENSION))
				|| (suf.equalsIgnoreCase(PRJ_EXTENSION))) {
			OutFileName1 = OutFileName.substring(0, OutFileName.length() - 4);
			OutFileName1 = OutFileName.concat(DBF_EXTENSION);
		}
		// otherwise if the suffix is not .shx, make it .shx
		else if (!suf.equalsIgnoreCase(DBF_EXTENSION)) {
			OutFileName1 = OutFileName.concat(DBF_EXTENSION);
		}

		// write the '.dbf' file.
		try {
			WriteDbfFile(OutFileName1);
		} catch (IOException ex) {
			logger.error("Could not write '.dbf' file", ex);
		}

		// if file ends with .shp or .shx or .dbf, strip the suffix and
		// append .prj
		if ((suf.equalsIgnoreCase(SHP_EXTENSION)) || (suf.equalsIgnoreCase(SHX_EXTENSION))
				|| (suf.equalsIgnoreCase(DBF_EXTENSION))) {
			OutFileName1 = OutFileName.substring(0, OutFileName.length() - 4);
			OutFileName1 = OutFileName.concat(PRJ_EXTENSION);
		}
		// otherwise if the suffix is not .shx, make it .shx
		else if (!suf.equalsIgnoreCase(PRJ_EXTENSION)) {
			OutFileName1 = OutFileName.concat(PRJ_EXTENSION);
		}

		// write out '.prj' file
		// Modified by Tenzing Shaw to check whether prj information exists
		if(!this._shapeObject.getIsInPixel()){
			try {
				WritePRJFile(OutFileName);
			} catch (IOException e) {
				logger.error("Could not write '.prj' file", e);
			}
		}

	}

	/**
	 * SHOULD WE HAVE THIS METHOD ?? Saves a shapefile to a file defined by a file name after converting the pixels to
	 * latitude and longitude
	 */
	/*  public boolean Write(String OutFileName, GeoConvert geoConvert) throws IOException {
	  // The OutFileName should not have the '.shp' extn.
	    boolean ret;
	    try{
	 if(geoConvert != null){
	   System.out.println("Converting pixels to latitude and longitude");
	   _geoConvert = geoConvert;
	 }
	 return( Write(OutFileName));
	    }catch(Exception e){
	  System.err.println("Error while writing out shape files"+e);
	  return false;
	    }
	  }
	 */

	/**
	 * Saves data from ShapeObject to a shape file defined by a file name
	 */
	public void Write(String OutFileName, ShapeObject obj) throws IOException, ShapefileException {
		setShapeObject(obj);
		this.Write(OutFileName);
	}

	/**
	 * Saves a shapefile to an output stream. This writes out the .shp file only.
	 * 
	 * @param file
	 *            A LEDataInputStream that conects to the shapefile to read When using this method the '.shx' and '.dbf'
	 *            files are NOT generated.
	 */
	private synchronized void writeShapefile(OutputStream os) throws IOException {

		// NOTE: When using this method the '.shx' and '.dbf' files are NOT generated.

		// create the output stream
		LEDataOutputStream file = null;
		BufferedOutputStream out = new BufferedOutputStream(os);
		file = new LEDataOutputStream(out);

		// write the main header
		_mainHeader.write(file);
		int offset = 50; // header length in WORDS
		// records;
		// body;
		// header;
		logger.debug("Saving: " + _records.size() + " records");

		// write out each record
		int numRecords = _records.size();
		for (int i = 0; i < numRecords; i++) {
			ShapeRecord item = (ShapeRecord) _records.get(i);
			// write out the header
			item.getHeader().write(file);
			// write out the shape data
			item.getShape().write(file);
		}
		file.flush();
		file.close();
	}

	/**
	 * Write out the .shx file
	 * 
	 * @param iname
	 * @throws IOException
	 */
	private void WriteIndexFile(String iname) throws IOException {
		// The iname should not have the '.shx' extn.
		// try {
		String suffix = iname.substring(iname.length() - 4, iname.length());
		if (!(suffix.equalsIgnoreCase(SHX_EXTENSION))) {
			FileOutputStream os = new FileOutputStream(iname.concat(SHX_EXTENSION));
			writeIndex(os);
		} else {
			FileOutputStream os = new FileOutputStream(iname);
			writeIndex(os);
		}
		// return (writeIndex(os));
		/*}
		     catch (Exception e) {
		  System.err.println("Error with output file: " + e);
		  return false;
		     }*/
	}

	/**
	 * Write out the .prj file
	 * 
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	private void WritePRJFile(String fileName) throws IOException {
		ShapeObject shapeObject = this.getShapeObject();
		Projection proj = shapeObject.getProjection();
		// PRJWriter writer=new PRJWriter(proj);
		String suffix = fileName.substring(fileName.length() - 4, fileName.length());
		if ((suffix.equalsIgnoreCase(SHX_EXTENSION)) || (suffix.equalsIgnoreCase(SHP_EXTENSION))
				|| (suffix.equalsIgnoreCase(DBF_EXTENSION))) {
			fileName = fileName.substring(0, fileName.length() - 4).concat(PRJ_EXTENSION);
		} else if (!(suffix.equalsIgnoreCase(PRJ_EXTENSION))) {
			fileName = fileName.concat(PRJ_EXTENSION);
		}
		try {
			// writer.write(fileName);
			PrintStream ps = new PrintStream(fileName);
			ps.print(proj.toStringPRJ());
		} catch (IOException e) {
			throw e;
		}
	}

	/**
	 * Write out the .shx file
	 * 
	 * @param os
	 * @return
	 * @throws IOException
	 */
	private synchronized void writeIndex(OutputStream os) throws IOException {

		// create the LEDataOutputStream
		BufferedOutputStream out = new BufferedOutputStream(os);
		LEDataOutputStream file = new LEDataOutputStream(out);

		logger.debug("Writing index file");

		// write the main header
		_mainHeader.writeToIndex(file); // ShapeFileHeader
		int offset = 50;
		int contentLen = 0;
		file.setLittleEndianMode(false);

		int numRecords = _records.size();
		// write out each record
		for (int i = 0; i < numRecords; i++) {
			ShapeRecord item = (ShapeRecord) _records.get(i);
			item.mainindex = offset; // Added by Sunayana
			file.writeInt(item.mainindex); // this should be the offset into the shp file
			contentLen = item.getHeader().getContentLength(); // In 16 bits WORD
			logger.debug("Writing index Record no: " + i + " at offset: " + offset);
			logger.debug("ContentLength: " + contentLen);
			file.writeInt(contentLen);
			offset += contentLen + 4; // Increase length by 16 bits WORDS
		}
		file.flush();
		file.close();
	}

	/**
	 * Gets the number of records stored in this shapefile
	 * 
	 * @return Number of records
	 */
	/*  public int getRecordCount() {
	    return _records.size();
	  }*/

	/*  public ArrayList getRecords() {
	    return _records;
	  }
	  public void setRecords(ArrayList records) {
	    _records = records;
	  }*/

	/**
	 * Get the data from the shapefile in ShapeObject format
	 * 
	 * @return a ShapeObject
	 * @deprecated use getShapeObject instead
	 */
	@Deprecated
	public ShapeObject GetShapeObject() {
		return getShapeObject();
	}

	/**
	 * Get the data from the shapefile in ShapeObject format
	 * 
	 * @return a ShapeObject
	 */
	public ShapeObject getShapeObject() {
		// If _shapeObject == null, create a new shapeObject.
		// Otherwise return the old _shapeObject.
		// convert records to ShapeObject.
		if (_shapeObject == null) {
			// sanity check
			if (_records == null) {
				logger.error("ERROR: no data available");
				return null;
			}
			int numShapes = _records.size();
			// The constructor will allocate memory for most of the member variables.
			_shapeObject = new ShapeObject(numShapes);

			// read in and initialize projection information associated with the object.
			// _shapeObject.setProjection(
			// ( new ProjectionLoaderPRJ( _shpFileName ) ).getProjection() );

			// do only in the case when that info is available (no exceptions thrown).
			try {
				// TODO parameters should be set here
				String prjfile = _shpFileName.replaceAll("\\.shp$", ".prj");
				Projection proj = PRJLoader.getProjection(new File(prjfile));
				if (proj == null) {
					proj = Projection.getProjection(ProjectionType.Geographic, new GeoGraphicCoordinateSystem(
							Datum.WGS_1984));
				}
				_shapeObject.setProjection(proj);
			} catch (Exception e) {
				logger.debug("Exception loading prj file.", e);
				try {
					_shapeObject.setProjection(Projection.getProjection(ProjectionType.Geographic,
							new GeoGraphicCoordinateSystem(Datum.WGS_1984)));
				} catch (GeoException e1) {
					logger.warn("Could not create projection.", e1);
				}
			}

			// We have to allocate memory for the _shapeObject._bndPts Point2DDouble.
			int bndIdx = 0; // to keep account of the index of each boundary.
			double[][] bndPts = new double[numShapes][];
			int[][] bndParts = new int[numShapes][];
			int totalNumPts = 0;
			int totalNumParts = 0;

			_shapeObject.setGlobalBoundingBox(_globalBndBox);

			int j;
			for (int i = 0; i < _records.size(); i++) {
				ShapeRecord item = (ShapeRecord) _records.get(i); // ShapeRecord's record header not required.
				ShapefileShape shape = item.getShape();

				// We use (i+1) as the actual boundaries are now stored from index 1.
				_shapeObject.setBoundaryPointsIndex(i, (bndIdx << 1)); // should be set to the starting index of each
				// boundary.
				// Since each point has a 'x' and 'y' coordinate, we must double the index
				_shapeObject.setNumBoundaryParts(i, shape.getNumParts()); // The number of parts in this boundary.
				_shapeObject.setBoundaryType(i, shape.getShapeType()); // The type of this boundary.
				_shapeObject.setNumBoundaryPoints(i, shape.getNumPoints()); // The number of points of this boundary
				_shapeObject.setBoundingBox(i, shape.getBoundingBox());
				// NOTE: numParts and Parts[] for a shape are ignored right now
				// Adding these few lines to test if Parts[] can be stored as seperate boundaries.

				double[] tempPoints = shape.getPoints();
				int halfPoints = shape.getNumPoints() << 1;
				// bndPts[i] = new double[ (shape.getNumPoints() << 1)];
				if (halfPoints <= 0) {
					logger.debug("WARNING: halfPoints <=0; shape.getNumPoints()=" + shape.getNumPoints() + ", val="
							+ halfPoints);
					bndPts[i] = new double[0];
					continue;
				}
				bndPts[i] = new double[halfPoints];
				if (shape.getNumParts() != 0) { // when a boundary has parts
					bndParts[i] = shape.getParts();
					totalNumParts += shape.getNumParts();
				} else { // For ShapePoint boundary type
					bndParts[i] = null; // is null for ShapePoint.
				}
				totalNumPts += shape.getNumPoints();

				// for (j = 0; (j < (shape.getNumPoints() << 1)); j++) {
				for (j = 0; j < halfPoints; j++) {
					bndPts[i][j] = tempPoints[j];
				}
				bndIdx += shape.getNumPoints();
			}
			// Copy all the points to _shapeObject._bndPts.ptsDouble
			// _shapeObject._bndPts = new Point2DDouble(totalNumPts);
			Point2DDouble p2d = new Point2DDouble(totalNumPts);
			p2d.maxValidPts = p2d.numpts;
			// _shapeObject._bndPts.maxValidPts = _shapeObject._bndPts.numpts;
			_shapeObject.setBoundaryPoints(p2d);
			int k = 0;
			for (int i = 0; i < numShapes; i++) {
				for (j = 0; j < bndPts[i].length; j++) {
					// _shapeObject._bndPts.ptsDouble[k] = bndPts[i][j];
					p2d.ptsDouble[k] = bndPts[i][j];
					k++;
				}
			}
			// Copy the Parts information for all the boundaries.
			// _shapeObject._bndPartsIdx = new int[totalNumParts];
			int[] bndPartsIdx = new int[totalNumParts];
			k = 0;
			for (int i = 0; i < numShapes; i++) {
				for (j = 0; j < _shapeObject.getNumBoundaryParts(i); j++) {
					// for (j = 0; j < bndParts[i].length; j++) {
					// When a boundary has 0 parts, we don't store anything for
					// it in _shapeObject._bndPartsIdx[]
					// _shapeObject._bndPartsIdx[k] = bndParts[ (i - 1)][j];
					bndPartsIdx[k] = bndParts[i][j];
					k++;
				}
			}
			_shapeObject.setBoundaryAllPartsIndex(bndPartsIdx);
		}
		return _shapeObject;
	}

	/**
	 * Make the data of this object be the data contained in obj
	 * 
	 * @param obj
	 * @throws IOException
	 * @throws ShapefileException
	 */
	private void setShapeObject(ShapeObject obj) throws IOException, ShapefileException {

		// HAVE TO SET THE HEADER FOR THE Shapefile
		_mainHeader = new ShapefileHeader();
		_shapeObject = obj;
		// convert ShapeObject to records
		_records = new ArrayList(_shapeObject.getNumBoundaries());
		ShapefileShape body;
		RecordHeader header;
		int allRecordsLength = 0;
		int[] parts;
		double[] bbox = new double[4];
		// double [] globalBndBox = new double[4];
		// boolean firstRecord = true;
		// Since the ShapeObject has a default boundary at index 0, we must retrieve points
		// starting from index 1.
		for (int i = 0; i < _shapeObject.getNumBoundaries(); i++) {
			switch (_shapeObject.getBoundaryType(i)) {
			// Get points for the ith boundary starting from the _bndIdx[i] of
			// _bndPts.ptsDouble upto (2 * _numBndPts[i]) to get both the x
			// and y values.

			case (POINT):
				double[] ptsPOINT;
				bbox = _shapeObject.getBoundingBox(i);
				ptsPOINT = _shapeObject.getPointsForBoundary(i);
				// x and y are stored as column and row in Point2DDouble.
				// So we must reverse the order before writing them to shapefile.
				body = new ShapePoint(ptsPOINT[1], ptsPOINT[0]);
				header = new RecordHeader(i, body); // CHECK THIS!!!!!!!
				break;
			case (ARC):
				bbox = _shapeObject.getBoundingBox(i);
				Point2DDouble ptsARC = new Point2DDouble(_shapeObject.getNumBoundaryPts(i));
				ptsARC.maxValidPts = _shapeObject.getNumBoundaryPts(i);
				ptsARC.ptsDouble = _shapeObject.getPointsForBoundary(i);
				parts = _shapeObject.getPartsForBoundary(i);
				body = new ShapeArc(bbox, parts, ptsARC);
				header = new RecordHeader(i, body); // CHECK THIS!!!!!!!
				break;
			case (POLYGON):
				bbox = _shapeObject.getBoundingBox(i);
				Point2DDouble ptsPOLYGON = new Point2DDouble(_shapeObject.getNumBoundaryPts(i));
				ptsPOLYGON.maxValidPts = _shapeObject.getNumBoundaryPts(i);
				ptsPOLYGON.ptsDouble = _shapeObject.getPointsForBoundary(i);
				parts = _shapeObject.getPartsForBoundary(i);
				body = new ShapePolygon(bbox, parts, ptsPOLYGON);
				header = new RecordHeader(i, body); // CHECK THIS!!!!!!!
				break;
			case (ARC_M):
				bbox = _shapeObject.getBoundingBox(i);
				Point2DDouble ptsARC_M = new Point2DDouble(_shapeObject.getNumBoundaryPts(i));
				ptsARC_M.maxValidPts = _shapeObject.getNumBoundaryPts(i);
				ptsARC_M.ptsDouble = _shapeObject.getPointsForBoundary(i);
				parts = _shapeObject.getPartsForBoundary(i);
				body = new ShapeArcM(bbox, parts, ptsARC_M);
				header = new RecordHeader(i, body); // CHECK THIS!!!!!!!
				break;
			default:
				throw new ShapeTypeNotSupportedException("Shape type: " + getShapeTypeDescription() + " [ "
						+ _shapeObject.getBoundaryType(i) + " ] not supported");
			} // switch(_shapeObject.GetBndType(i))

			// Record header must be set in individual case statements
			// as we want to get the content length for each shape
			// under consideration
			_records.add(new ShapeRecord(header, body));
			allRecordsLength += 4 + body.getLength();
			/* THIS SHOULD NOT BE NEEDED AFTER WE ADDED THE globalBndBox to ShapeObject
			if(bbox != null){ // Set the global bounding box for the shapefile
			if(firstRecord){
			       globalBndBox[0] = bbox[0];
			       globalBndBox[1] = bbox[1];
			       globalBndBox[2] = bbox[2];
			       globalBndBox[3] = bbox[3];
			       firstRecord = false;
			} // if(firstRecord)
			if(globalBndBox[0] > bbox[0])
			       globalBndBox[0] = bbox[0]; // minX
			if(globalBndBox[1] > bbox[1])
			       globalBndBox[1] = bbox[1]; // minY
			if(globalBndBox[2] < bbox[2])
			       globalBndBox[2] = bbox[2]; // maxX
			if(globalBndBox[3] < bbox[3])
			       globalBndBox[3] = bbox[3]; // maxY
			 }// if(bbox != null)
			*/
		} // for( i = ...)

		// Set the header fields
		// Assuming that a file will always contain the same kind of boundaries.
		_mainHeader.setShapeType(obj.getBoundaryType(1));
		_mainHeader.setBBox(_shapeObject.getGlobalBoundingBox());
		_mainHeader.setVersion(VERSION);
		_mainHeader.setFileCode(FILE_CODE);
		_mainHeader.setFileLength(50 + allRecordsLength);
		_mainHeader.setIndexLength(50 + (4 * _records.size()));
	}

	/*  public boolean InsertIntoRecords(ArrayList fromRecords,
	                                   double[] globalBBox) {
	    if (fromRecords == null) {
	      System.err.println("No records to insert to shapefile _records!");
	      return false;
	    }
	    if (_records == null) {
	      System.out.println("Creating new shapefile _record...");
	      _records = new ArrayList(fromRecords.size());
	    }
	    if (_mainHeader == null) {
	      _mainHeader = new ShapefileHeader();
	      _mainHeader.setFileLength(50); // a file will have at least this length 'coz of the shapefile header
	      _mainHeader.setIndexLength(50); // minimum length of index file
	    }
	    int i;
	    if (_globalBndBox == null) {
	      _globalBndBox = new double[4];
	      for (i = 0; i < 4; i++) {
	        _globalBndBox[i] = globalBBox[i];
	      }
	    }
	    if (_globalBndBox[0] > globalBBox[0]) { // minX
	      _globalBndBox[0] = globalBBox[0];
	    }
	    if (_globalBndBox[1] > globalBBox[1]) { // minY
	      _globalBndBox[1] = globalBBox[1];
	    }
	    if (_globalBndBox[2] < globalBBox[2]) { // maxX
	      _globalBndBox[2] = globalBBox[2];
	    }
	    if (_globalBndBox[3] < globalBBox[3]) { // maxY
	      _globalBndBox[3] = globalBBox[3];
	    }
	    ShapeRecord item;
	    int allRecordsLength = 0;
	    item = (ShapeRecord) fromRecords.get(0);
	    int type = item.shape.getShapeType(); // Assuming all boundaries of a shapefile are of the same type.
	    if (_mainHeader.getShapeType() != -1) { // 1 shapefile has already been inserted...
	      if (_mainHeader.getShapeType() != type) {
	        System.err.println(
	     "Warning: Trying to merge shapefiles of different boundary types");
	      }
	    }
	    for (i = 0; i < fromRecords.size(); i++) {
	      item = (ShapeRecord) fromRecords.get(i);
	      _records.add(new ShapeRecord(item.header, item.shape));
	      allRecordsLength += 4 + item.shape.getLength();
	    }
	    _mainHeader.setShapeType(type);
	    _mainHeader.setBBox(_globalBndBox);
	    _mainHeader.setVersion(VERSION);
	    _mainHeader.setFileCode(FILE_CODE);
	    int currFileLength = _mainHeader.getFileLength();
	    currFileLength += allRecordsLength;
	    _mainHeader.setFileLength(currFileLength);
	    int currIndexLength = _mainHeader.getIndexLength();
	    currIndexLength += (fromRecords.size() << 2);
	    _mainHeader.setIndexLength(currIndexLength);
	    // System.out.println("Num records after an insertion = "+_records.size());
	    return true;
	  }*/

	/*  public void setShapefileHeader(ShapefileHeader header) {
	    _mainHeader = header;
	  }*/

	/**
	 * Returns a ShapefileShape If index is out of range a null ShapefileShape will be returned. (As an alternative I
	 * could throw an ArrayIndexOutOfBoundsException, comments please...)
	 * 
	 * @param index
	 *            The index of the record from which to extract the shape.
	 * @return A ShapefileShape from the given index.
	 */
	/*  public ShapefileShape getShape(int index) {
	    ShapeRecord r;
	    try {
	      r = (ShapeRecord) _records.get(index);
	    }
	    catch (java.lang.ArrayIndexOutOfBoundsException e) {
	      return null;
	    }
	    return r.getShape();
	  }*/

	/**
	 * Returns an array of all the shapes in this shapefile.
	 * 
	 * @return An array of all the shapes
	 */
	/*  public ShapefileShape[] getShapes() {
	    ShapefileShape[] shapes = new ShapefileShape[_records.size()];
	    ShapeRecord r;
	    for (int i = 0; i < _records.size(); i++) {
	      r = (ShapeRecord) _records.get(i);
	      // JUST TO CHECK
	      logger.debug("Record shape: " + r.getShapeType());
	      shapes[i] = r.getShape();
	    }
	    return shapes;
	  }*/

	/**
	 * Gets the bounding box for the whole shape file.
	 * 
	 * @return An array of four doubles in the form {x1,y1,x2,y2}
	 */
	/*  public double[] getBounds() {
	    return _mainHeader.getBounds();
	  }*/

	/**
	 * Gets the type of shape stored in this shapefile.
	 * 
	 * @return An int indicating the type
	 * @see #getShapeTypeDescription()
	 * @see #getShapeTypeDescription(int type)
	 */
	/*  public int getShapeType() {
	    return _mainHeader.getShapeType();
	  }*/

	/**
	 * Returns a string for the shape type of index.
	 * 
	 * @param index
	 *            An int coresponding to the shape type to be described
	 * @return A string descibing the shape type
	 */
	private static String getShapeTypeDescription(int index) {
		switch (index) {
		case (NULL):
			return ("Null");
		case (POINT):
			return ("Points");
		case (ARC):
			return ("Arcs");
		case (ARC_M):
			return ("ArcsM");
		case (POLYGON):
			return ("Polygons");
		case (MULTIPOINT):
			return ("Multipoints");
		case (ShapefileLoader.POLY_LINE_Z):
			return ("PolyLineZ");
		default:
			return ("Undefined");
		}
	}

	/**
	 * Returns a description of the shape type stored in this shape file.
	 * 
	 * @return String containing description
	 */
	private String getShapeTypeDescription() {
		return getShapeTypeDescription(_mainHeader.getShapeType());
	}

	/*  public boolean ReadIndex(String filename) throws java.io.IOException,
	      ShapefileException {
	    // The filename should not have the '.shx' extn.
	    FileInputStream in = null;
	    try {
	     String suf = filename.substring(filename.length() - 4, filename.length());
	      if (suf.equalsIgnoreCase(".shx")) {
	        in = new FileInputStream(filename);
	      }
	      else {
	        in = new FileInputStream(filename.concat(".shx"));
	      }
	      return (readIndex(in));
	    }
	    catch (Exception exp) {
	      System.err.println("No Input Index File to read: " + filename);
	      return false;
	    }
	  }*/

	/*  private synchronized boolean readIndex(InputStream is) throws IOException {
	LEDataInputStream file = null;
	try {
	  BufferedInputStream in = new BufferedInputStream(is);
	  file = new LEDataInputStream(in);
	}
	catch (Exception e) {
	  System.err.println(e);
	  return false;
	}
	logger.debug("Reading index file header:");
	ShapefileHeader header = new ShapefileHeader(file); //Reads the header from the file
	int offset = 0, len = 0;
	file.setLittleEndianMode(false);
	for (int i = 0; i < _records.size(); i++) {
	  logger.debug("Sf-->Reading index Record");
	  ShapeRecord item = (ShapeRecord) _records.get(i);
	  logger.debug("Sf-->Offset: " + file.readInt());
	  logger.debug("Sf-->ContentLength: " + file.readInt());
	}
	file.close();
	return true;
	}*/

	/**
	 * Write a dbf file out.
	 * 
	 * @param OutFileName
	 * @throws IOException
	 */
	private void WriteDbfFile(String OutFileName) throws IOException {

		DBFLoader dbfLoader = new DBFLoader();
		dbfLoader.addField("AREA", DBFLoader.INT, 13, 0);
		dbfLoader.addField("PERIMETER", DBFLoader.INT, 14, 0);
		dbfLoader.addField("PGAPOL_", DBFLoader.INT, 12, 0);
		dbfLoader.addField("PGAPOL_ID", DBFLoader.INT, 12, 0);
		dbfLoader.addField("GRID_CODE", DBFLoader.INT, 12, 0);
		dbfLoader.addField("VALUE", DBFLoader.INT, 14, 0);

		// The OutFileName should not have th 'dbf' extn.
		/*    DbfFieldDef[] dbfFld;
		    try {
		      dbfFld = new DbfFieldDef[6];
		      dbfFld[0] = new DbfFieldDef("AREA", 'N', 13, 0);
		      dbfFld[1] = new DbfFieldDef("PERIMETER", 'N', 14, 0);
		      dbfFld[2] = new DbfFieldDef("PGAPOL_", 'N', 12, 0);
		      dbfFld[3] = new DbfFieldDef("PGAPOL_ID", 'N', 12, 0);
		      dbfFld[4] = new DbfFieldDef("GRID_CODE", 'N', 12, 0);
		      dbfFld[5] = new DbfFieldDef("VALUE", 'N', 14, 0);
		      DbfFileWriter dbfFile;*/

		String suf = OutFileName.substring(OutFileName.length() - 4, OutFileName.length());
		if (suf.equalsIgnoreCase(DBF_EXTENSION)) {
			// dbfFile = new DbfFileWriter(OutFileName);
			dbfLoader.open(OutFileName, DBFLoader.WRITE);
		} else {
			// dbfFile = new DbfFileWriter(OutFileName.concat(DBF_EXTENSION));
			dbfLoader.open(OutFileName.concat(DBF_EXTENSION), DBFLoader.WRITE);
		}

		int numRecords = _records.size();
		logger.debug("Will write  " + numRecords + " records to .dbf file");
		// Vector[] records = new Vector[numRecords];

		Object[][] records = new Object[numRecords][6];

		for (int i = 0; i < numRecords; i++) {
			// records[i] = new Vector(6);
			records[i][0] = new Integer(0);
			records[i][1] = new Integer(0);
			records[i][2] = new Integer(i + 2);
			records[i][3] = new Integer(i + 1);
			records[i][4] = new Integer(1);
			records[i][5] = new Integer(1);
		}

		// dbfFile.writeHeader(dbfFld, numRecords); // an array for fld definitions and the number of records
		dbfLoader.writeRecord(records, numRecords);
		dbfLoader.close();
		// return true;
		/*}
		         catch (DbfFileException de) {
		  System.err.println("DBFFile error: " + de);
		  return false;
		         }
		         catch (Exception e) {
		  logger.error("Error: " + e);
		  return false;
		         }*/
		// return true;
	}

	/*  public boolean MergeShape(Shapefile spf2, String MergeFileName,
	                            int mergeDelta) {
	    ArrayList records2 = spf2.getRecords();
	    ArrayList newRecords = new ArrayList();
	    _mainHeader = new ShapefileHeader();
	    _mergeDelta = mergeDelta;
	    int recordCount = 0;
	    RecordHeader header;
	    ShapefileShape combinedShape;
	    for (int i = 0; i < _records.size(); i++) {
	      // Compare each record with all the records of spf2, one by one.
	      // MUST also compare parts if there are any.
	      ShapeRecord item1 = (ShapeRecord) _records.get(i);
	      for (int j = 0; j < records2.size(); j++) {
	        ShapeRecord item2 = (ShapeRecord) records2.get(j);
	        if (item2.getShapeType() != item1.getShapeType()) {
	          continue; // If shapeTypes don't match, don't compare them
	        }
	        if (item2.getShapeType() == Shapefile.POINT) {
	     continue; // CHECK...Assuming that we don't need to merge in such a case
	        }
	        ShapefileShape shape1 = item1.getShape();
	        ShapefileShape shape2 = item2.getShape();
	        combinedShape = CompareAndCombine(shape1, shape2);
	     if (combinedShape != null) { // Merge and write the two into the MergeFileName
	          System.out.println("Match found");
	          recordCount++;
	          //CHECK THIS!!! What if the a record from one file
	          // matches 2 records from the other file.
	     header = new RecordHeader( (recordCount), combinedShape); //CHECK THIS!!!!!!!
	          newRecords.add(new ShapeRecord(header, combinedShape));
	          // May have to save these as new records and call the ContourShape.WriteContourShapeFile()
	          // to write out to the output file.
	        } // if(match)
	      } // for j
	    } // for i
	    if (newRecords.size() != 0) {
	      createMergedShapefile(MergeFileName, newRecords);
	      // Write out the newRecords
	      // Can't use Contour2Shapefile class as it can only deal
	      // with contour data.
	      // We might have some other ShapefileShape like a ShapePolygon.
	      // This may require an additional writer.
	    }
	    return true;
	  } // MergeShape
	 */

	/*  private ShapefileShape CompareAndCombine(ShapefileShape shape1,
	                                           ShapefileShape shape2) {
	    // Compares the endpoints of shape1 and shape2.
	    // If a match is found, it will create a new ShapefileShape object.
	    // Otherwise it returns a null
	    int shapeType = shape1.getShapeType();
	    ShapefileShape resultShape = null;
	    boolean bothends = false;
	    boolean left1right2 = false;
	    boolean left1left2 = false;
	    boolean right1right2 = false;
	    boolean right1left2 = false;
	    boolean match = false;
	    int numResParts = 0;
	    int numResPoints = 0;
	    double[][] tempResPoints = new double[shape1.getNumParts()][]; // This is the max possible size
	    double[] bbox = new double[4];
	    boolean firstBBoxValue = true;
	    MERGED_FILE_SHAPETYPE = shapeType; // Set this for writin in the new shapefile's header
	    if (shapeType != ARC && shapeType != ARC_M && shapeType != POLYGON) {
	      System.err.println(
	          "Unsupported type of shape. Cannot compare and combine");
	      return null;
	    }
	    // The final shape will have all the points that matched, taking the parts into consideration.
	    int[] parts1 = shape1.getParts();
	    double[] points1 = shape1.getPoints();
	    int[] parts2 = shape2.getParts();
	    double[] points2 = shape2.getPoints();
	    for (int i = 0; i < shape1.getNumParts(); i++) {
	      // Compare the end points of each part
	      double[] endpoints1 = getEndPoints(points1, parts1, i);
	      System.out.println("In CompareAndCombine(): Num Parts1: " +
	                         shape1.getNumParts() + " num Parts2: " +
	                         shape2.getNumParts());
	      for (int j = 0; j < shape2.getNumParts(); j++) {
	        double[] endpoints2 = getEndPoints(points2, parts2, j);
	        int k = 0, l = 0, p, q;
	        for (k = 0; k < 2; k++) { // checking the left end points
	          if (Math.abs(endpoints1[k] - endpoints2[k]) > _mergeDelta) {
	            break;
	          }
	        } // for k
	        for (l = 2; l < 4; l++) { // checking the right end points
	          if (Math.abs(endpoints1[l] - endpoints2[l]) > _mergeDelta) {
	            break;
	          }
	        } // for l
	        if (l == 4 && k != 2) {
	          right1right2 = true;
	        }
	        if (k == 2 && l != 4) {
	          left1left2 = true;
	        }
	        if (l == 4 && k == 2) {
	          bothends = true;
	          //Also check the right with left and the left with right end points.
	        }
	        for (p = 0, q = 2; p < 2 && q < 4; p++, q++) {
	          // Checking left of 1 with right of 2.
	          if (Math.abs(endpoints1[p] - endpoints2[q]) > _mergeDelta) {
	            break;
	          }
	        }
	        if (p == 2 && q == 4) {
	          left1right2 = true;
	        }
	        for (p = 0, q = 2; p < 2 && q < 4; p++, q++) {
	          // Checking right endpoint of 1 with left of 2
	          if (Math.abs(endpoints1[q] - endpoints2[p]) > _mergeDelta) {
	            break;
	          }
	        }
	        if (p == 2 && q == 4) {
	          right1left2 = true;
	        }
	        if (!bothends && !right1left2 && !left1right2 && !left1left2 &&
	            !right1right2) { // None of the endpoints match
	          continue; // Continue with the next part
	        }
	        double[] partPts1 = shape1.getPartPoints(i);
	        double[] partPts2 = shape2.getPartPoints(j);
	        tempResPoints[numResParts] = new double[ (partPts1.length) +
	            (partPts2.length)];
	        numResPoints += (partPts1.length) + (partPts2.length);
	        if (firstBBoxValue) {
	          bbox[0] = endpoints1[0]; // minX
	          bbox[1] = endpoints1[1]; //minY
	          bbox[2] = bbox[0]; // maxX
	          bbox[3] = bbox[1]; //maxY
	          firstBBoxValue = false;
	        }
	        // Order of filling the new points will differ depending on which endpoints had matched
	        if (bothends) { // Both left and right end points match
	          match = true;
	          // Start filling points from 0 to n of partPts2 and then from n to 0 of partPts1
	          int m = 0;
	          for (int n = 0; n < partPts2.length; n++) {
	            tempResPoints[numResParts][m] = partPts2[n];
	            //System.out.println("m= "+m+" tempresPoints[m] = "+tempResPoints[numResParts][m]);
	            // if m is even then it is a 'y' coordinate, otherwise and 'x' coordinate
	            if ( (m % 2) != 0) { // m is odd
	     bbox[0] = getLesser(bbox[0], tempResPoints[numResParts][m]); // MinX
	     bbox[2] = getGreater(bbox[2], tempResPoints[numResParts][m]); //MaxX
	            }
	            if ( (m % 2) != 0) { // m is even
	     bbox[1] = getLesser(bbox[1], tempResPoints[numResParts][m]); // MinY
	     bbox[3] = getGreater(bbox[3], tempResPoints[numResParts][m]); //MaxY
	            }
	            m++;
	          }
	          for (int n = partPts1.length / 2; n > 0; n--) { //CHECKING THIS PART!! COPY FROM SOME OTHER PART IF WRONG
	            // When inserting in opposite direction...x should be in odd positions
	            tempResPoints[numResParts][m] = partPts1[ (n << 1) - 2];
	            m++;
	            tempResPoints[numResParts][m] = partPts1[ (n << 1) - 1];
	            //if( (m%2) != 0){ // m is odd
	     bbox[0] = getLesser(bbox[0], tempResPoints[numResParts][m]); // MinX
	     bbox[2] = getGreater(bbox[2], tempResPoints[numResParts][m]); //MaxX
	            //}
	            //if( (m%2) != 0){ // m is even
	     bbox[1] = getLesser(bbox[1], tempResPoints[numResParts][m - 1]); // MinY
	     bbox[3] = getGreater(bbox[3], tempResPoints[numResParts][m - 1]); //MaxY
	            //}
	            m++;
	          }
	        } // Both end points match.
	        if (left1left2 && !match) {
	          // Start filling from n to 0 of partPts2 and then from 0 to n of partPts1
	          System.out.println("In left1left2");
	          int m = 0;
	          for (int n = partPts2.length / 2; n > 0; n--) { //CHECKING THIS PART!! COPY FROM SOME OTHER PART IF WRONG
	            // When inserting in opposite direction...x should be in odd positions
	            tempResPoints[numResParts][m] = partPts2[ (n << 1) - 2];
	            m++;
	            tempResPoints[numResParts][m] = partPts2[ (n << 1) - 1];
	     bbox[0] = getLesser(bbox[0], tempResPoints[numResParts][m]); // MinX
	     bbox[2] = getGreater(bbox[2], tempResPoints[numResParts][m]); //MaxX
	     bbox[1] = getLesser(bbox[1], tempResPoints[numResParts][m - 1]); // MinY
	     bbox[3] = getGreater(bbox[3], tempResPoints[numResParts][m - 1]); //MaxY
	            m++;
	          }
	          for (int n = 0; n < partPts1.length; n++) {
	            tempResPoints[numResParts][m] = partPts1[n];
	            if ( (m % 2) != 0) { // m is odd
	     bbox[0] = getLesser(bbox[0], tempResPoints[numResParts][m]); // MinX
	     bbox[2] = getGreater(bbox[2], tempResPoints[numResParts][m]); //MaxX
	            }
	            if ( (m % 2) != 0) { // m is even
	     bbox[1] = getLesser(bbox[1], tempResPoints[numResParts][m]); // MinY
	     bbox[3] = getGreater(bbox[3], tempResPoints[numResParts][m]); //MaxY
	            }
	            m++;
	          }
	        }
	        if (right1right2 && !match) {
	          // Start filling from 0 to n of partPts1 and then from n to 0 of partPts2
	          System.out.println("In right1right2");
	          int m = 0;
	          for (int n = 0; n < partPts1.length; n++) {
	            tempResPoints[numResParts][m] = partPts1[n];
	            if ( (m % 2) != 0) { // m is odd
	     bbox[0] = getLesser(bbox[0], tempResPoints[numResParts][m]); // MinX
	     bbox[2] = getGreater(bbox[2], tempResPoints[numResParts][m]); //MaxX
	            }
	            if ( (m % 2) != 0) { // m is even
	     bbox[1] = getLesser(bbox[1], tempResPoints[numResParts][m]); // MinY
	     bbox[3] = getGreater(bbox[3], tempResPoints[numResParts][m]); //MaxY
	            }
	            m++;
	          }
	          for (int n = partPts2.length / 2; n > 0; n--) { //CHECKING THIS PART!! COPY FROM SOME OTHER PART IF WRONG
	            // When inserting in opposite direction...x should be in odd positions
	            tempResPoints[numResParts][m] = partPts2[ (n << 1) - 2];
	            m++;
	            tempResPoints[numResParts][m] = partPts2[ (n << 1) - 1];
	     bbox[0] = getLesser(bbox[0], tempResPoints[numResParts][m]); // MinX
	     bbox[2] = getGreater(bbox[2], tempResPoints[numResParts][m]); //MaxX
	     bbox[1] = getLesser(bbox[1], tempResPoints[numResParts][m - 1]); // MinY
	     bbox[3] = getGreater(bbox[3], tempResPoints[numResParts][m - 1]); //MaxY
	            m++;
	          }
	        }
	        if (left1right2 && !match) {
	          // Start filling from 0 to n of partPts2 and then from 0 to n of partPts1
	          System.out.println("In left1right2");
	          int m = 0;
	          for (int n = 0; n < partPts2.length; n++) {
	            tempResPoints[numResParts][m] = partPts2[n];
	            if ( (m % 2) != 0) { // m is odd
	     bbox[0] = getLesser(bbox[0], tempResPoints[numResParts][m]); // MinX
	     bbox[2] = getGreater(bbox[2], tempResPoints[numResParts][m]); //MaxX
	            }
	            if ( (m % 2) != 0) { // m is even
	     bbox[1] = getLesser(bbox[1], tempResPoints[numResParts][m]); // MinY
	     bbox[3] = getGreater(bbox[3], tempResPoints[numResParts][m]); //MaxY
	            }
	            m++;
	          }
	          for (int n = 0; n < partPts1.length; n++) {
	            tempResPoints[numResParts][m] = partPts1[n];
	            if ( (m % 2) != 0) { // m is odd
	     bbox[0] = getLesser(bbox[0], tempResPoints[numResParts][m]); // MinX
	     bbox[2] = getGreater(bbox[2], tempResPoints[numResParts][m]); //MaxX
	            }
	            if ( (m % 2) != 0) { // m is even
	     bbox[1] = getLesser(bbox[1], tempResPoints[numResParts][m]); // MinY
	     bbox[3] = getGreater(bbox[3], tempResPoints[numResParts][m]); //MaxY
	            }
	            m++;
	          }
	        }
	        if (right1left2 && !match) {
	          // Start filling from 0 to n of partPts1 and then from 0 to n of partPts2
	          System.out.println("In right1left2");
	          int m = 0;
	          for (int n = 0; n < partPts1.length; n++) {
	            tempResPoints[numResParts][m] = partPts1[n];
	            if ( (m % 2) != 0) { // m is odd
	     bbox[0] = getLesser(bbox[0], tempResPoints[numResParts][m]); // MinX
	     bbox[2] = getGreater(bbox[2], tempResPoints[numResParts][m]); //MaxX
	            }
	            if ( (m % 2) != 0) { // m is even
	     bbox[1] = getLesser(bbox[1], tempResPoints[numResParts][m]); // MinY
	     bbox[3] = getGreater(bbox[3], tempResPoints[numResParts][m]); //MaxY
	            }
	            m++;
	          }
	          for (int n = 0; n < partPts2.length; n++) {
	            tempResPoints[numResParts][m] = partPts2[n];
	            if ( (m % 2) != 0) { // m is odd
	     bbox[0] = getLesser(bbox[0], tempResPoints[numResParts][m]); // MinX
	     bbox[2] = getGreater(bbox[2], tempResPoints[numResParts][m]); //MaxX
	            }
	            if ( (m % 2) != 0) { // m is even
	     bbox[1] = getLesser(bbox[1], tempResPoints[numResParts][m]); // MinY
	     bbox[3] = getGreater(bbox[3], tempResPoints[numResParts][m]); //MaxY
	            }
	            m++;
	          }
	        }
	        numResParts++;
	      } // for j
	    } // for i
	    // Populate and combine to form new shape
	    // Deal with:
	    //          The bounding box.
	    //          The Parts
	    //          Filelength
	    if (numResParts == 0) {
	      System.out.println("No match found in CompareAndCombine");
	      return null;
	    }
	    // Set the global bbox that will be written to the new file after mergeShape
	    if (_firstGBoxValue) {
	      _mergedGlobalBBox[0] = bbox[0]; // minX
	      _mergedGlobalBBox[1] = bbox[1]; //minY
	      _mergedGlobalBBox[2] = _mergedGlobalBBox[0]; // maxX
	      _mergedGlobalBBox[3] = _mergedGlobalBBox[1]; //maxY
	      _firstGBoxValue = false;
	    }
	    else {
	     _mergedGlobalBBox[0] = getLesser(_mergedGlobalBBox[0], bbox[0]); // MinX
	     _mergedGlobalBBox[1] = getLesser(_mergedGlobalBBox[1], bbox[1]); // MinY
	     _mergedGlobalBBox[2] = getLesser(_mergedGlobalBBox[2], bbox[2]); // MaxX
	     _mergedGlobalBBox[3] = getLesser(_mergedGlobalBBox[3], bbox[3]); // MaxY
	    }
	    numResPoints = numResPoints / 2; // Taking x and y as one point
	    double[] allPoints = new double[numResPoints << 1];
	     int[] allResParts = new int[numResParts]; // Should this be numResParts - 1 ????
	    int k = 0;
	    int partIdx = 0;
	    for (int i = 0; i < numResParts; i++) {
	      for (int j = 0; j < tempResPoints[i].length; j++) {
	        allPoints[k] = tempResPoints[i][j];
	        k++;
	      }
	      allResParts[i] = partIdx;
	      partIdx += ( (tempResPoints[i].length) / 2); // Taking x and y as one
	    }
	    // Get bounding box.
	    Point2DDouble allResPoints = new Point2DDouble();
	    allResPoints.maxValidPts = numResPoints;
	    allResPoints.numpts = numResPoints;
	    allResPoints.ptsDouble = allPoints;
	    System.out.println("points[0]: " + allResPoints.ptsDouble[0] +
	                       " points[1]: " + allResPoints.ptsDouble[1] +
	                       " points[length - 2]: " +
	     allResPoints.ptsDouble[allResPoints.ptsDouble.length - 2] +
	                       " points[length - 1]: " +
	     allResPoints.ptsDouble[allResPoints.ptsDouble.length - 1]);
	    switch (shapeType) {
	      case ARC:
	        resultShape = new ShapeArc(bbox, allResParts, allResPoints);
	        break;
	      case POLYGON:
	        resultShape = new ShapePolygon(bbox, allResParts, allResPoints);
	        break;
	      case ARC_M:
	        resultShape = new ShapeArcM(bbox, allResParts, allResPoints);
	        break;
	      default:
	        System.err.println("Unsupported Type! Cannot be merged");
	        return null;
	    }
	    // Calculate the records' length that will be written to the new file after merge.
	    _mergedRecordsLength += 4 + resultShape.getLength();
	    return resultShape;
	  }
	 */

	private static double getGreater(double a, double b) {
		// Just gives the greater of a and b. Called by CompareAndCombine
		if (a > b) {
			return a;
		} else {
			return b;
		}
	}

	private static double getLesser(double a, double b) {
		// Just gives the lesser of a and b. Called by CompareAndCombine
		if (a < b) {
			return a;
		} else {
			return b;
		}
	}

	/*  private double[] getEndPoints(double[] points, int[] parts, int index) {
	    // Returns an array of double of size 4(for x and y) for the 2 endpoints of the a part
	    // in points[] located at 'index' of parts[].
	    // CHECK !!!!!!!! That the x and y values are not mixed up while here or while storing them
	    // !!!!!!!!! when are are first read from file/contour data
	    if (index > parts.length) {
	      System.err.println("Index:" + index + " out of bounds!");
	      return null;
	    }
	    double[] endpoints = new double[4];
	    endpoints[0] = points[ (parts[index] << 1) + 1]; // The left x point.
	    endpoints[1] = points[ (parts[index] << 1)]; // the left y point.
	    if (index == (parts.length - 1)) { // The last part.
	      int pointsLength = points.length;
	      endpoints[2] = points[pointsLength - 1]; // The right x point.
	      endpoints[3] = points[pointsLength - 2]; // The right y point.
	      for (int i = 0; i < 4; i++) {
	        System.out.println("Endpoints: " + endpoints[i]);
	      }
	      return endpoints;
	    }
	    // When the index is not the last one in parts[].
	    // Left endpoint is the one at parts[index] in points[].
	    // Right endpoint is the one at (parts[index+1] - 1) in points[].
	    int ptsUptoIdx = parts[index + 1] - 1;
	    endpoints[2] = points[ (ptsUptoIdx << 1) + 1]; // The right x point.
	    endpoints[3] = points[ptsUptoIdx << 1]; // The right y point.
	    for (int i = 0; i < 4; i++) {
	      System.out.println("Endpoints: " + endpoints[i]);
	    }
	    return endpoints;
	  }
	 */
	/*  public boolean createMergedShapefile(String OutFileName,
	                                       ArrayList newRecords) {
	    // The OutFileName should NOT have the '.shp' extn.
	    // This method also generates the '.shx' and the '.dbf' files for OutFileName.
	    //sanity check
	    if (OutFileName == null) {
	      System.out.println("ERROR: missing file name");
	      return false;
	    }
	    String suf = OutFileName.substring(OutFileName.length() - 4,
	                                       OutFileName.length());
	    if (suf.equalsIgnoreCase(".shp")) {
	      //Strip the suffix
	      OutFileName = OutFileName.substring(0, OutFileName.length() - 4);
	    }
	    if (newRecords == null || newRecords.size() <= 0) {
	      System.out.println("ERROR: missing data or <=0 size");
	      return false;
	    }
	    boolean ret;
	    try {
	      _mainHeader.setFileLength(50 + _mergedRecordsLength);
	      _mainHeader.setBBox(_mergedGlobalBBox);
	      _mainHeader.setVersion(VERSION);
	      _mainHeader.setFileCode(FILE_CODE);
	      _mainHeader.setShapeType(MERGED_FILE_SHAPETYPE);
	      _mainHeader.setIndexLength(50 + (4 * newRecords.size()));
	      setRecords(newRecords);
	      ret = Write(OutFileName);
	      return ret;
	    }
	    catch (Exception e) {
	     System.err.println("Error from Shapefile.WriteMergedShapeFile(): " + e);
	      return false;
	    }
	  } //WriteMergedShapeFile
	 */
} // Shapefile

/**
 * 
 */
