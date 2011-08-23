//package ncsa.i2k.io.dlg;
package edu.illinois.ncsa.isda.imagetools.core.io.dlg;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ShapeObject;
import edu.illinois.ncsa.isda.imagetools.core.geo.GeoException;
import edu.illinois.ncsa.isda.imagetools.core.geo.projection.AlbersEqualAreaConic;
import edu.illinois.ncsa.isda.imagetools.core.geo.projection.ModelProjection;
import edu.illinois.ncsa.isda.imagetools.core.geo.projection.UTMNorth;
import edu.illinois.ncsa.isda.imagetools.core.io.shapefile.ShapefileLoader;
import edu.illinois.ncsa.isda.imagetools.core.io.shapefile.ShapefileShape;
import edu.illinois.ncsa.isda.imagetools.core.io.util.LEDataOutputStream;
import gnu.trove.TDoubleArrayList;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntObjectHashMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/**
 * A DLG3 loader. This will load the specified DLG3 file into a shape object.
 * DLG3 locations are expressed in either albers or UTM. Since there is no
 * metadata associated with a shape object, this reader will convert the points
 * directly into latitude and longitude points and store them in the shape
 * object in lat/lng.
 * 
 * @author clutter
 * @todo COMPUTE GLOBAL BOUNDING BOX!
 * @todo read the paraemters for albers from the file-they are hard-coded now
 */
public class DLG3Loader {

	// ////////////////////////////////////////////////////
	// shape object support

	/**
	 * the global bounding box is the boundary around the entire shape
	 */
	double[]					globalBoundingBox			= new double[4];

	/**
	 * A list of the types of boundaries in this shape. For DLG3, this will only
	 * be Polygons.
	 */
	TIntArrayList				boundaryTypes				= new TIntArrayList();

	/**
	 * The bounding box for each boundary. There will be 4 entries for each
	 * boundary.
	 */
	TDoubleArrayList			boundingBoxes				= new TDoubleArrayList();

	/**
	 * The number of points in each boundary.
	 */
	TIntArrayList				numberOfPointsInBoundary	= new TIntArrayList();

	/**
	 * The actual points for all the boundaries.
	 */
	TDoubleArrayList			boundaryPoints				= new TDoubleArrayList();

	/**
	 * index into boundaryPoints for each boundary.
	 */
	TIntArrayList				boundaryPointsIndex			= new TIntArrayList();

	/**
	 * the number of parts in each boundary
	 */
	TIntArrayList				numberOfPartsInEachBoundary	= new TIntArrayList();

	/**
	 * The index for the parts of a boundary.
	 */
	TIntArrayList				boundaryPartsIndex			= new TIntArrayList();

	// end shape object support
	// //////////////////////////////////////

	/**
	 * The major code for each boundary
	 */
	TIntArrayList				minorCodes					= new TIntArrayList();

	/**
	 * The minor code for each boundary
	 */
	TIntArrayList				majorCodes					= new TIntArrayList();

	private List				categories;
	private int					UTMZone;

	private double				SWEasting;
	private double				SWNorthing;

	private double				NWEasting;
	private double				NWNorthing;

	private double				NEEasting;
	private double				NENorthing;

	private double				SEEasting;
	private double				SENorthing;

	// the key is the major attribute code
	// the value is an int[] of the rows of the table with this major attribute
	// private TIntObjectHashMap majorLookup;

	// private HashMap codeToDescriptionLookup;

	private TIntObjectHashMap	lineMap;
	// this projection is used to convert the points to lat/lng
	private ModelProjection		projection;

	// if true, the points are UTM, else they are albers
	private boolean				isUTM;

	protected ShapeObject		_shapeObject;

	/**
	 * Read the DLG at the specified file.
	 * 
	 * @param shapeFile
	 *            File
	 * @throws Exception
	 */
	public DLG3Loader(File shapeFile) throws Exception {
		this(new BufferedReader(new FileReader(shapeFile)));
	}

	/**
	 * Read the DLG from a zip file.
	 * 
	 * @param shapes
	 *            ZipEntry
	 * @param zipFile
	 *            ZipFile
	 * @throws Exception
	 */
	public DLG3Loader(ZipEntry shapes, ZipFile zipFile) throws Exception {
		this(new BufferedReader(new InputStreamReader(zipFile.getInputStream(shapes))));
	}

	private DLG3Loader(BufferedReader reader) throws Exception {
		init(reader);
	}

	/**
	 * Get the shape object.
	 * 
	 * @return The ShapeObject created by reading the DLG3 file.
	 */
	public ShapeObject GetShapeObject() {

		// if it has not been constructed, construct it now.
		// copy all the relevant data from DLG3 object into the shape object
		if (_shapeObject == null) {

			int numBoundaries = boundaryTypes.size();

			_shapeObject = new ShapeObject(numBoundaries, globalBoundingBox, boundaryTypes.toNativeArray(),
					boundingBoxes.toNativeArray(), numberOfPointsInBoundary.toNativeArray(),
					boundaryPoints.toNativeArray(), boundaryPointsIndex.toNativeArray(),
					numberOfPartsInEachBoundary.toNativeArray(), boundaryPartsIndex.toNativeArray(), null);

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

			_shapeObject.setGlobalBoundingBox(new double[] { minLongitude, minLatitude, maxLongitude, maxLatitude });
		}
		return _shapeObject;
	}

	private boolean setUTMConversionParameters(double[] bbox, int zone) {
		// 1 = UTM
		// geoImage.SetModelType(1);
		projection = new UTMNorth();
		// is this enough??
		// geoImage.SetEastingInsertionValue(bbox[0]);
		// geoImage.SetNorthingInsertionValue(bbox[1]);
		// geoImage.SetModelSpaceX(geoImage.GetEastingInsertionValue());
		// geoImage.SetModelSpaceY(geoImage.GetNorthingInsertionValue());
		// geoImage.SetUTMZone(zone);
		projection.SetEastingInsertionValue(bbox[0]);
		projection.SetNorthingInsertionValue(bbox[1]);
		projection.SetModelSpaceX(bbox[0]);
		projection.SetModelSpaceY(bbox[1]);
		((UTMNorth) projection).SetUTMZone(zone);

		return true;
	}

	/**
	 * LAM -- this is a hack; these parameters should be read from the file
	 * because they can vary!
	 * 
	 * @param bbox
	 *            double[]
	 * @return boolean
	 */
	private boolean setAlbersConversionParameters(double[] bbox) {
		// GeoImageObject geoImage = new GeoImageObject();
		projection = new AlbersEqualAreaConic();
		// geoImage.SetModelType(GeoImageObject.ALBERS_EQUAL_AREA_CONIC);
		// geoImage.SetFirstStandardParallel(29.5);
		// geoImage.SetSecondStandardParallel(45.5);
		// geoImage.SetGeoProjCenterLat(23);
		// geoImage.SetGeoProjCenterLng(-96);
		// geoImage.SetRadius(6378206.4);
		// geoImage.SetInverseFlat(294.9786982);
		// geoImage.SetEccentricitySqrd(0.00676866);
		// geoConvert = new GeoConvert(geoImage);

		// LAM --- these should be read from the file, not hard-coded!
		((AlbersEqualAreaConic) projection).setParallels(29.5, 45.5);

		// projection.SetGeoProjCenterLat(23);
		// projection.SetGeoProjCenterLng(-96);
		((AlbersEqualAreaConic) projection).setOrigin(23, -96);
		((AlbersEqualAreaConic) projection).setRadius(6378206.4);

		// projection.SetInverseFlat(294.9786982);
		((AlbersEqualAreaConic) projection).setEccentricSqure(0.00676866);

		return true;
	}

	/*
	 * private String[] getAttributes(int[] majorMinorPairs) { if (majorMinorPairs ==
	 * null || majorMinorPairs.length == 0) { return null; }
	 * 
	 * int numPairs = majorMinorPairs.length / 2; String[] retVal = new
	 * String[numPairs]; int idx = 0;
	 * 
	 * for (int i = 0; i < numPairs; i += 2) { int majorcode = majorMinorPairs[i];
	 * int minorcode = majorMinorPairs[i + 1];
	 *  // now look up the codes int[] rows = (int[]) majorLookup.get(majorcode); if
	 * (rows == null) { retVal[idx] = null; System.out.println("Major code not
	 * found!: " + majorcode + " : " + minorcode + " "); } else { boolean found =
	 * false; int rowidx = 0; while (!found) { int r = rows[rowidx]; int mc =
	 * attributeTable.getInt(r, MINOR); if (mc == minorcode) { retVal[idx] =
	 * attributeTable.getString(r, VALUE); found = true; } rowidx++; } } if
	 * (retVal[idx] == null) { System.out.println("Attribute Not Found: " +
	 * majorcode + " : " + minorcode); } idx++; }
	 *  // now transform these into mapunit values
	 * 
	 * HashMap map = new HashMap(); // for each mapunit value, code is the key,
	 * value is the value
	 * 
	 * return retVal; }
	 */

	/**
	 * Read in the data from the file. Would probably be clearer if this method
	 * was named "Read", but it is named init to stay as close as possible to
	 * the Shapefile API.
	 * 
	 * @param in
	 * @throws IOException
	 */
	private void init(BufferedReader reader) throws IOException {
		// int numHeaderLines = 0;

		// **************************************
		// File Identification and Description Records

		// ---------------------
		// record 1
		// data element 1 = banner
		String line = reader.readLine();
		// numHeaderLines++;

		// --------------------
		// record 2
		line = reader.readLine();
		// numHeaderLines++;

		// data element 1 = name of digital cartographic unit
		String nameOfDigitalCartographicUnit = line.substring(0, 40);
		// data element 2 = date of original source material
		String dateOfOriginalSourceMaterial = line.substring(41, 51);
		// data element 3 = collection procedure qualifier
		String collectionProcedureQualifier = line.substring(51, 52);
		// data element 4 = scale of original source material
		String scaleOfOriginalSourceMaterial = line.substring(52, 60);

		// data element 5 does not apply

		// ---------------------
		// record 3
		// none of the data elements apply
		line = reader.readLine();
		// numHeaderLines++;

		// --------------------
		// record 4
		line = reader.readLine();
		// numHeaderLines++;

		// data element 1 = DLG Level Code
		String dlgLevelCode = line.substring(0, 6);
		// data element 2 = code defining ground planimetric reference system
		String groundPlanimetricSystem = line.substring(6, 12);
		int val = Integer.parseInt(groundPlanimetricSystem.trim());
		if (val == 1) {
			isUTM = true;
		} else {
			isUTM = false;
		}

		// data element 3 = code defining zone in ground planimetric ref system
		String groundPlanimetricZoneCode = line.substring(12, 18);
		if (isUTM) {
			UTMZone = Integer.parseInt(groundPlanimetricZoneCode.trim());
		}

		// data element 4 = code defining units of measure for ground planimetric
		// coord
		// throughout the file
		String unitsOfMeasure = line.substring(18, 24);
		// data element 5 = resolution
		String resolution = line.substring(24, 42);
		// data element 6 = number of file-to-map transformation parameters
		String numFileToMapTransformParameters = line.substring(42, 48);
		// data element 7 = number of accuracy/miscellaneous records
		String numAccuracyMiscRecords = line.substring(48, 54);
		// data element 8 = number of control points (n)
		String numControlPoints = line.substring(54, 60);
		// data element 9 = number of categories in the DLG file (q)
		String numCategories = line.substring(60, 66);
		// data element 10 = horizonal datum
		String horizontalDatum = line.substring(66, 69);
		// data element 11 = vertical datum
		String verticalDatum = line.substring(69, 72);

		// --------------------------
		// records 5 - 9
		// projection parameters for map transformation
		for (int i = 5; i <= 9; i++) {
			reader.readLine();
		}

		// -----------------------------
		// record 10
		// internal file-to-map projection transformation parameters
		line = reader.readLine();

		// *****************************************
		// Control Point identification Records
		int n = Integer.parseInt(numControlPoints.trim());

		// 1-n
		for (int i = 0; i < n; i++) {
			line = reader.readLine();
			// SW, NW, NE, or SE for four quadrangle corners
			String controlPointLabel = line.substring(0, 6);

			// in degrees and decimal degress
			String latitude = line.substring(6, 18).trim();

			// in degress and decimal degrees
			String longitude = line.substring(18, 36).trim();

			// in units in the approx zone of the ground planimetric coordinate
			// system
			String easting = line.substring(36, 48).trim();

			// in units in the approx zone of the ground planimetric coordinate
			// system
			String northing = line.substring(48, 60).trim();

			controlPointLabel = controlPointLabel.trim();
			if (controlPointLabel.equals("SW")) {
				SWEasting = Double.parseDouble(easting);
				SWNorthing = Double.parseDouble(northing);
			} else if (controlPointLabel.equals("NW")) {
				NWEasting = Double.parseDouble(easting);
				NWNorthing = Double.parseDouble(northing);
			} else if (controlPointLabel.equals("NE")) {
				NEEasting = Double.parseDouble(easting);
				NENorthing = Double.parseDouble(northing);
			} else if (controlPointLabel.equals("SE")) {
				SEEasting = Double.parseDouble(easting);
				SENorthing = Double.parseDouble(northing);
			}
		}

		if (isUTM) {
			// fill in the BB in UTM
			double[] bbox = new double[] { SWEasting, SWNorthing, NEEasting, NENorthing };

			setUTMConversionParameters(bbox, UTMZone);
		} else {
			double[] bbox = new double[] {
			// SWEasting, SWNorthing, NEEasting, NENorthing};
					NWEasting, NWNorthing, SEEasting, SENorthing };
			setAlbersConversionParameters(bbox);
		}

		/*
		 * System.out.println("SWLng: "+SWLongitude+" SWLat: "+SWLatitude+" NELong:
		 * "+NELongitude+" NELat: "+NELatitude); Point2DDouble p2d = new
		 * Point2DDouble(2); p2d.ptsDouble[0] = SWNorthing; p2d.ptsDouble[1] =
		 * SWEasting; Point2DDouble t1 = geoConvert.UTMNorthingEasting2LatLng(p2d);
		 * System.out.println("conv SWNorthing: "+t1.ptsDouble[0]);
		 * System.out.println("conv SWEasting: "+t1.ptsDouble[1]);
		 */

		// *****************************************
		// Data Category identification Records
		int q = Integer.parseInt(numCategories.trim());

		categories = new LinkedList();

		// 1-q
		for (int i = 0; i < q; i++) {
			Category c = new Category();
			categories.add(c);

			line = reader.readLine();
			// data element 1 - category name
			// the first 4 characters are unique to USGS/NMD data
			String categoryName = line.substring(0, 20);
			c.name = categoryName;

			// data element 2 - attribute format codes
			// blank or zero indicates default attribute formatting in major-minor
			// pairs
			String attributeFormatCode = line.substring(20, 24).trim();
			c.attributeFormatCodes = Integer.parseInt(attributeFormatCode);

			// data element 3 - highest node id number
			// the number of nodes referenced in the file
			String highestNodeIDNumber = line.substring(24, 30).trim();
			c.highestNodeIDNumber = Integer.parseInt(highestNodeIDNumber);

			// data element 4 - actual number of nodes
			// only if DCF not packed, and element ID numbers not compressed
			// will this number be diff from data element 3
			String actualNumberOfNodesInFile = line.substring(30, 36).trim();
			c.actualNumberOfNodes = Integer.parseInt(actualNumberOfNodesInFile);

			// data element 5 - presence of node to area linkage records
			// 0 = node-area list not included, 1 = node-area list included
			String nodeToAreaLinkageRecords = line.substring(38, 39).trim();
			int tmp = Integer.parseInt(nodeToAreaLinkageRecords);
			if (tmp == 0) {
				c.presenceOfNodeToAreaLinkageRecords = false;
			} else if (tmp == 1) {
				c.presenceOfNodeToAreaLinkageRecords = true;

				// data element 6 - presence of node to line linkage records
				// 0 = node-line list not included, 1 = node-line list included
			}
			String nodeToLineLinkageRecords = line.substring(39, 40).trim();
			tmp = Integer.parseInt(nodeToLineLinkageRecords);
			if (tmp == 0) {
				c.presenceOfNodeToLineLinkageRecords = false;
			} else if (tmp == 1) {
				c.presenceOfNodeToLineLinkageRecords = true;

				// data element 7 - highest area id number
				// number of areas referenced in file
			}
			String highestAreaIDNumber = line.substring(40, 46).trim();
			c.highestAreaIDNumber = Integer.parseInt(highestAreaIDNumber);

			// data element 8 - actual number of areas
			// only if DCF is not packed, and the element ID numbers not compressed,
			// will this number be different from data element 7
			String actualNumberOfAreasInFile = line.substring(46, 52).trim();
			c.actualNumberOfAreas = Integer.parseInt(actualNumberOfAreasInFile);

			// data element 9 - presence of area to node linkage
			// 0 = area-node list not included, 1 = area-node list included
			String presenceOfAreaToNodeLinkageRecords = line.substring(53, 54).trim();
			tmp = Integer.parseInt(presenceOfAreaToNodeLinkageRecords);
			if (tmp == 0) {
				c.presenceOfAreaToNodeLinkageRecords = false;
			} else if (tmp == 1) {
				c.presenceOfAreaToNodeLinkageRecords = true;

				// data element 10 - presence of area to line linkage
				// 0 = area-line list not included, 1 = area-line list included
			}
			String presenceOfAreaToLineLinkageRecords = line.substring(54, 55).trim();
			tmp = Integer.parseInt(presenceOfAreaToLineLinkageRecords);
			if (tmp == 0) {
				c.presenceOfAreaToLineLinkageRecords = false;
			} else if (tmp == 1) {
				c.presenceOfAreaToLineLinkageRecords = true;

				// data element 11 - presence of area-coordinate lists
				// 0 = area-coordinate list not included, 1 = area-coordinate list
				// included
			}
			String presenceOfAreaToCoordinateLists = line.substring(55, 56).trim();
			tmp = Integer.parseInt(presenceOfAreaToCoordinateLists);
			if (tmp == 0) {
				c.presenceOfAreaCoordinateLists = false;
			} else if (tmp == 1) {
				c.presenceOfAreaCoordinateLists = true;

				// data element 12 - highest line id number
				// number of lines referenced in file
			}
			String highestLineIDNumber = line.substring(56, 62).trim();
			c.highestLineIDNumber = Integer.parseInt(highestLineIDNumber);

			// data element 13 - actual number of lines in file
			// only if DCF is not packed, and the element ID numbers not compressed,
			// will this number be diff from data element 12
			String actualNumberOfLinesInFile = line.substring(62, 68).trim();
			c.actualNumberOfLines = Integer.parseInt(actualNumberOfLinesInFile);

			// data element 14 - presence of line-coordinate lists
			// 0 = line coordinates not included, 1 = line coordinates included
			String presenceOfLineCoordinateLists = line.substring(71, 72).trim();
			tmp = Integer.parseInt(presenceOfLineCoordinateLists);
			if (tmp == 0) {
				c.presenceOfLineCoordinateLists = false;
			} else if (tmp == 1) {
				c.presenceOfLineCoordinateLists = true;
			}
		}

		// ******************************************
		// Node, Area, and Line identification records

		Category currentCategory = (Category) categories.get(0);
		List areas = new ArrayList();

		// now the rest are node, area, and line identification records
		// first read in the line records, as the area records rely on the lines

		lineMap = new TIntObjectHashMap();

		boolean done = false;
		while ((line = reader.readLine()) != null) {
			// nodes are never really needed... don't read them in.
			/*
			 * if (line.charAt(0) == N) { }
			 */
			if (line.charAt(0) == A) {
				// this is an area
				// AreaRecord area = new AreaRecord();
				// currentCategory.areas.add(area);

				// the id number
				String IDNumber = line.substring(1, 6).trim();
				int ID = Integer.parseInt(IDNumber);
				// the coordinates - this is a coordinate representative of the area
				// this is not really needed
				// String coordinates = line.substring(6, 30).trim();
				// StringTokenizer strtok = new StringTokenizer(coordinates);

				// get the x coord
				/*
				 * double xCoord; try { String x = strtok.nextToken(); xCoord =
				 * Double.parseDouble(x.trim()); } catch (Exception ex) { xCoord = 0; }
				 */

				// get the y coord
				/*
				 * double yCoord; try { String y = strtok.nextToken(); yCoord =
				 * Double.parseDouble(y.trim()); } catch (Exception ex) { yCoord = 0; }
				 */

				// get the number of elements in the node list
				// This is always zero for SSURGO data
				/*
				 * String numberElementsInNodeList = line.substring(30, 36).trim(); int
				 * numElementsInNodeList; try { numElementsInNodeList =
				 * Integer.parseInt(numberElementsInNodeList); } catch
				 * (NumberFormatException ex) { numElementsInNodeList = 0; }
				 */

				// get the number of elements in the line list
				String numberElementsInLineList = line.substring(36, 42).trim();
				int numElementsInLineList;
				try {
					numElementsInLineList = Integer.parseInt(numberElementsInLineList);
				} catch (NumberFormatException ex) {
					numElementsInLineList = 0;
				}

				// get the number of xy pts
				// this is always zero for SSURGO data
				/*
				 * String numberXYPoints = line.substring(42, 48).trim(); int
				 * numPointsInAreaCoordinateList; try { numPointsInAreaCoordinateList =
				 * Integer.parseInt(numberXYPoints); } catch (NumberFormatException ex) {
				 * numPointsInAreaCoordinateList = 0; }
				 */

				// the number of attribute code pairs
				String numberAttributeCodePairs = line.substring(48, 54).trim();
				int numAttributeCodePairsListed;
				try {
					numAttributeCodePairsListed = Integer.parseInt(numberAttributeCodePairs);
				} catch (NumberFormatException ex) {
					numAttributeCodePairsListed = 0;
				}

				String numberOfIslandsWithinArea = line.substring(60, 66).trim();
				int numIslands;
				try {
					numIslands = Integer.parseInt(numberOfIslandsWithinArea);
				} catch (Exception ex) {
					numIslands = 0;
				}

				// read in the node list
				// area.nodeList = readIDList(numElementsInNodeList, reader);

				// read in the line list
				int[] lineList = readIDList(numElementsInLineList, reader);

				// read in the xy pts
				// area.coordinateList = readCoordinateList(
				// numPointsInAreaCoordinateList, reader);

				// read in the attribute code pairs
				int[] attributeList = null;
				attributeList = readAttributeCodeList(numAttributeCodePairsListed, reader);

				// String[] attributes = getAttributes(attributeList);

				// if(attributeList != null && attributeList.length > 0) {
				AreaRecord area = new AreaRecord(ID, lineList, attributeList);
				areas.add(area);
				// }
			} // if A

			else if (line.charAt(0) == L) {

				// this is a line
				String IDNumber = line.substring(1, 6).trim();
				int ID = Integer.parseInt(IDNumber);

				// the starting node
				// String startingNode = line.substring(6, 12).trim();
				// int start = Integer.parseInt(startingNode);

				// the ending node
				// String endingNode = line.substring(12, 18).trim();
				// int end = Integer.parseInt(endingNode);

				// the left area
				/*
				 * String leftArea = line.substring(18, 24).trim(); int left; try { left =
				 * Integer.parseInt(leftArea); } catch (NumberFormatException ex) { left =
				 * 0; } // the right area String rightArea = line.substring(24,
				 * 30).trim(); int right; try { right = Integer.parseInt(rightArea); }
				 * catch (NumberFormatException ex) { right = 0; }
				 */
				// number of xy points listed
				String numberXYPoints = line.substring(42, 48).trim();
				int numXYPoints;
				try {
					numXYPoints = Integer.parseInt(numberXYPoints);
				} catch (NumberFormatException ex) {
					numXYPoints = 0;
				}

				// number of attribute code pairs listed
				String numberAttributeCodePairs = line.substring(48, 54).trim();
				int numAttrCodes;
				try {
					numAttrCodes = Integer.parseInt(numberAttributeCodePairs);
				} catch (NumberFormatException ex) {
					numAttrCodes = 0;
				}

				// read in the xy pts
				double[] coordinateList = readCoordinateList(numXYPoints, reader);

				// read in the attribute code pairs
				int[] attributeCodeList = readAttributeCodeList(numAttrCodes, reader);

				// create a new line record
				LineRecord lineRec = new LineRecord(ID, coordinateList);

				lineMap.put(lineRec.ID, lineRec);
			} // if == L
		}

		int num = areas.size();

		for (int i = 0; i < num; i++) {
			AreaRecord area = (AreaRecord) areas.get(i);
			area.init();

			// add the area's type
			boundaryTypes.add(area.getShapeType());

			// add the area's bounding box
			// double[] bb = area.GetBndBox();
			double[] bb = area.getBoundingBox();
			int lengthOfBB = bb.length;
			for (int z = 0; z < lengthOfBB; z++) {
				boundingBoxes.add(bb[z]);
			}

			// set the number of points for the boundary
			int numPoints = area.getNumPoints();
			numberOfPointsInBoundary.add(numPoints);

			// add the points
			double[] points = area.getPoints();
			int currentIndex = boundaryPoints.size();
			boundaryPoints.add(points);

			// set the index into boundaryPoints. this is where the points
			// for this line record begin
			boundaryPointsIndex.add(currentIndex);

			/**
			 * the number of parts in each boundary
			 */
			int numParts = area.getNumParts();
			numberOfPartsInEachBoundary.add(numParts);

			/**
			 * The index for the parts of a boundary.
			 */
			boundaryPartsIndex.add(area.getParts());

			majorCodes.add(area.attributeList[MAJOR]);
			minorCodes.add(area.attributeList[MINOR]);

			// make the attribute table here!
			// int[] attributeList = area.attributeList;

			/*
			 * for(int j = 0; j < attributeList.length; j++) {
			 * attributeTable.setInt(attributeList[j], i, j); }
			 */

			// String[] atts = getAttributes(area.attributeList);
			// LAM -- assuming only one attribute...
			// attributeCodes.add(atts[0]);
			/**
			 * the attribute code for each boundary
			 */
			// dlgAttributeCodes.add(area.attributeCodeList);
			/*
			 * String[] atts = area.attributes; int len = atts.length; for (int z =
			 * 0; z < len; z++) { String desc = (String)
			 * codeToDescriptionLookup.get(atts[z]); if (desc != null) {
			 * attributeCodes.add(desc); } else { attributeCodes.add(atts[z]); } }
			 */
		}
	} // init

	/**
	 * Read in a list of IDs. An ID is an int value.
	 * 
	 * @param numElements
	 *            the number of IDs to read in
	 * @param reader
	 *            the reader to read data from
	 * @return a TIntArrayList containing the IDs
	 * @throws IOException
	 */
	private static int[] readIDList(int numElements, BufferedReader reader) throws IOException {
		if (numElements == 0) {
			return null;
		}
		// TIntArrayList list = new TIntArrayList(numElements);
		int[] list = new int[numElements];

		int lineCounter = 0;
		boolean ok = (lineCounter == numElements);
		while (!ok) {
			String line = reader.readLine();
			/*
			 * StringTokenizer tok = new StringTokenizer(line); while
			 * (tok.hasMoreElements()) { String token = tok.nextToken();
			 * //list.add(Integer.parseInt(token)); list[lineCounter] =
			 * Integer.parseInt(token); lineCounter++; if (lineCounter == numElements) { ok =
			 * true; } }
			 */

			// every 6 bytes is a number
			int len = line.length();
			int curLoc = 0;
			while (curLoc < len) {
				String num = line.substring(curLoc, curLoc + 6);
				curLoc += 6;
				list[lineCounter] = Integer.parseInt(num.trim());
				lineCounter++;
			}
			if (lineCounter == numElements) {
				ok = true;
			}
		}
		return list;
	}

	/**
	 * Read a list of coordinates. Right now, read in each indiviual coordinate
	 * and later turn them into (x,y) points. This is inefficient .. could make
	 * the (x,y) points on the fly..
	 * 
	 * @param numCoordinates
	 *            the number of (x,y) coordinates to read
	 * @param reader
	 *            the reader to read data from
	 * @return an ArrayList containing Points for these coordinates
	 * @throws IOException
	 */
	private double[] readCoordinateList(int numCoordinates, BufferedReader reader) throws IOException {
		if (numCoordinates == 0) {
			return null;
		}

		// each coordinate has an x and y component, so we are actually reading in
		// twice as many elements
		int numElements = numCoordinates * 2;
		// String[] coords = new String[numElements];
		double[] coords = new double[numElements];

		int lineCounter = 0;
		boolean ok = (lineCounter == numElements);
		while (!ok) {
			String line = reader.readLine();
			StringTokenizer tok = new StringTokenizer(line);
			/*
			 * while (tok.hasMoreElements()) { //String token = tok.nextToken();
			 * //list.add(Integer.parseInt(token)); coords[lineCounter] = tok.nextToken();
			 * lineCounter++; if (lineCounter == numElements) { ok = true; } }
			 */
			// every 12 bytes is a number
			int len = line.length();
			int curLoc = 0;
			while (curLoc < len) {
				String num = line.substring(curLoc, curLoc + 12);
				curLoc += 12;
				coords[lineCounter] = Double.parseDouble(num.trim());
				lineCounter++;
			}
			if (lineCounter == numElements) {
				ok = true;
			}
		}

		// now take the coordinates and turn them into points.
		double[] list = new double[numElements];
		// int ctr = 0;
		double[] northingEasting = new double[2];
		for (int i = 0; i < numElements; i += 2) {
			/*
			 * double easting; double northing; try { easting =
			 * Double.parseDouble(coords[i].trim()); } catch (Exception ex) {
			 * easting = 0; } try { northing = Double.parseDouble(coords[i +
			 * 1].trim()); } catch (Exception ex) { ex.printStackTrace(); northing =
			 * 0; }
			 */

			// list[i] = easting;
			// list[i + 1] = northing;
			list[i] = coords[i];
			list[i + 1] = coords[i + 1];

			northingEasting[0] = list[i + 1];
			northingEasting[1] = list[i];

			try {
				double[] latlng = projection.modelToEarth(northingEasting);
				list[i + 1] = latlng[1];
				list[i] = latlng[0];
			} catch (GeoException ex) {

			}
		}

		return list;
	}

	private static int[] readAttributeCodeList(int numElements, BufferedReader reader) throws IOException {

		TIntArrayList retVal = new TIntArrayList(numElements * 2);

		int numElem = numElements;
		boolean zeros = true;

		if (numElements > 0) {

			String line = reader.readLine();
			line = line.trim();

			// the last six characters are the minor code, the first characters are
			// the major code

			int len = line.length();

			String major = line.substring(0, len - 6).trim();
			String minor = line.substring(len - 6).trim();
			// System.out.println("major: "+major+" minor: "+minor);

			retVal.add(Integer.parseInt(major));
			retVal.add(Integer.parseInt(minor));

			zeros = (retVal.get(0) == 0) && (retVal.get(1) == 0);
		}
		if (zeros) {
			return new int[0];
		}
		return retVal.toNativeArray();
	}

	private static final char	N	= 'N';
	private static final char	A	= 'A';
	private static final char	L	= 'L';

	/**
	 * A record for an Area
	 * <p>
	 * Title:
	 * </p>
	 * <p>
	 * Description:
	 * </p>
	 * <p>
	 * Copyright: Copyright (c) 2004
	 * </p>
	 * <p>
	 * Company:
	 * </p>
	 * 
	 * @author not attributable
	 * @version 1.0
	 */
	private class AreaRecord implements ShapefileShape {
		/** the ID for this area */
		int					ID;

		private int[]		parts;
		private double[]	points;
		private double[]	boundingBox;
		private final int[]	attributeList;
		private final int[]	lines;

		AreaRecord(int idNumber, int[] lines, int[] attributes) {

			ID = idNumber;
			this.lines = lines;
			attributeList = attributes;
		}

		public int[] getAttributeList() {
			return attributeList;
		}

		void init() {
			// an area is made up of lines.

			// '0' is a delimiter for an island

			// each closed ring (delimited by 0) will be a part
			List lineParts = new LinkedList();

			int numLines = lines.length;
			// the list of lines that make up a part
			TIntArrayList part = new TIntArrayList();

			LinkedList set = new LinkedList();

			// look at each line ID
			for (int i = 0; i < numLines; i++) {
				// get the line ID
				int num = lines[i];

				// if it is non-zero, add it to the part
				if (num != 0) {
					// look up the line, get the number of points
					LineRecord lr = (LineRecord) lineMap.get(Math.abs(num));
					part.add(num);
				}
				// otherwise if it is zero, this is the delimiter between parts
				// everything that came before was one part.
				// then clear, so we can start constructing the next part
				else {
					int[] ar = part.toNativeArray();
					lineParts.add(ar);
					part.clear();
				}
			}

			// pick up the last part
			if (part.size() != 0) {
				int[] ar = part.toNativeArray();
				lineParts.add(ar);
				part.clear();
			}

			// the number of parts
			int numParts = lineParts.size();

			// now, for each part, get the points and check the bounding box
			double minLongitude = Double.POSITIVE_INFINITY;
			double maxLongitude = Double.NEGATIVE_INFINITY;
			double minLatitude = Double.POSITIVE_INFINITY;
			double maxLatitude = Double.NEGATIVE_INFINITY;

			// cycle through the coordinates for this line
			// to populate the points[] array and find the bounding box

			// the points
			TDoubleArrayList pts = new TDoubleArrayList();
			int curIndex = 0;
			// the index of each part in the pts array
			TIntArrayList partsIndex = new TIntArrayList();

			// iterate over the parts
			ListIterator iter = lineParts.listIterator();

			while (iter.hasNext()) {
				partsIndex.add(curIndex / 2);

				// these are a list of the IDs for each line in the part
				int[] prt = (int[]) iter.next();
				int numlines = prt.length;

				// for each line in the part
				for (int i = 0; i < numlines; i++) {
					int line = prt[i];
					LineRecord record = (LineRecord) lineMap.get(Math.abs(line));

					double[] linePoints;
					int len;
					linePoints = record.getPoints();
					len = linePoints.length;
					if (line < 0) {
						linePoints = reverse(linePoints);
					}

					// examine all the points in the line
					for (int j = 0; j < len; j += 2) {
						double longitude = linePoints[j + 1];
						double latitude = linePoints[j];

						if (longitude < minLongitude) {
							minLongitude = longitude;
						}
						if (longitude > maxLongitude) {
							maxLongitude = longitude;
						}
						if (latitude < minLatitude) {
							minLatitude = latitude;
						}
						if (latitude > maxLatitude) {
							maxLatitude = latitude;
						}
					} // for j

					// add the points for the line
					if (i > 0) {
						pts.add(linePoints, 2, linePoints.length - 2);
						curIndex += linePoints.length - 2;
					} else {
						pts.add(linePoints);
						curIndex += linePoints.length;
					}
				} // for i

				// now we have a PART. a PART is a closed ring.
				// check that it is closed here!
			} // while

			parts = partsIndex.toNativeArray();
			points = pts.toNativeArray();

			/*
			 * java.awt.geom.Point2D.Double p1 = new
			 * java.awt.geom.Point2D.Double(points[0], points[1]);
			 * java.awt.geom.Point2D.Double p2 = new
			 * java.awt.geom.Point2D.Double(points[points.length-2],
			 * points[points.length-1]); if(!p1.equals(p2) && points.length < 40) {
			 * System.out.println("**UNCLOSED AREA. "+parts.length+" "+ID); for(int
			 * i = 0; i < points.length; i += 2) {
			 * System.out.println(points[i]+","+points[i+1]); }
			 * System.out.println(); System.out.println();
			 *  }
			 */

			// the bounding box
			boundingBox = new double[] { minLongitude, minLatitude, maxLongitude, maxLatitude };
		}

		/**
		 * the type of shape
		 * 
		 * @return
		 */
		public int getShapeType() {
			return ShapefileLoader.POLYGON;
		}

		/**
		 * a line is made up of two points
		 */
		public int getNumPoints() {
			return points.length / 2;
		}

		/**
		 * a line is made up of one part --- two points connected by a single line
		 */
		public int getNumParts() {
			return parts.length;
		}

		// the index of the first part in the points array
		public int[] getParts() {
			return parts;
		}

		// the points
		public double[] getPoints() {
			return points;
		}

		// since there is only one part, the part points are the same as all the
		// points
		public double[] getPartPoints(int part) {
			// get the points only for a particular part.

			int partPointsBegin = parts[part] * 2;
			int partPointsEnd;
			if (part == parts.length - 1) {
				partPointsEnd = points.length;
			} else {
				partPointsEnd = parts[part + 1] * 2;
			}

			int numPoints = partPointsEnd - partPointsBegin;
			double[] pts = new double[numPoints];
			int idx = 0;
			for (int i = partPointsBegin; i < partPointsEnd; i++) {
				pts[idx] = points[i];
				idx++;
			}

			return pts;
		}

		// the bounding box
		public double[] getBoundingBox() {
			return boundingBox;
		}

		// we will never use this, leave blank
		public boolean resetPoints(double[] points) {
			return true;
		}

		// will never use this, leave blank
		public void write(LEDataOutputStream file) throws java.io.IOException {
		}

		// will never use this, leave blank
		public int getLength() {
			return 0;
		}
	}

	/**
	 * A record for a line
	 * <p>
	 * Title:
	 * </p>
	 * <p>
	 * Description:
	 * </p>
	 * <p>
	 * Copyright: Copyright (c) 2004
	 * </p>
	 * <p>
	 * Company:
	 * </p>
	 * 
	 * @author not attributable
	 * @version 1.0
	 */
	private class LineRecord implements ShapefileShape {

		/** the ID for this line */
		private final int		ID;

		/** the coordinates */
		private double[]		coordinateList	= null;

		private final int[]		parts;

		private final double[]	boundingBox;

		LineRecord(int idNumber, double[] coords) {
			ID = idNumber;
			coordinateList = coords;

			double minLongitude = Double.POSITIVE_INFINITY;
			double maxLongitude = Double.NEGATIVE_INFINITY;
			double minLatitude = Double.POSITIVE_INFINITY;
			double maxLatitude = Double.NEGATIVE_INFINITY;

			// cycle through the coordinates for this line
			// to populate the points[] array and find the boudning box

			int numCoordinates = coordinateList.length;
			for (int i = 0; i < numCoordinates; i += 2) {
				// Coordinate coord = coordinateList[idx];
				double lat = coordinateList[i];
				double lng = coordinateList[i + 1];

				if (lng < minLongitude) {
					minLongitude = lng;
				}
				if (lng > maxLongitude) {
					maxLongitude = lng;
				}
				if (lat < minLatitude) {
					minLatitude = lat;
				}
				if (lat > maxLatitude) {
					maxLatitude = lat;
				}
				// the points are stored lat/lng
				// points[i] = coord.latitude;
				// points[i + 1] = coord.longitude;
				// idx++;
			}

			// there is one part for the line
			parts = new int[] { 0 };

			// the bounding box
			boundingBox = new double[] { minLongitude, minLatitude, maxLongitude, maxLatitude };
		}

		/**
		 * the type of shape
		 * 
		 * @return
		 */
		public int getShapeType() {
			return ShapefileLoader.ARC;
		}

		/**
		 * the number of points in the line
		 */
		public int getNumPoints() {
			// return points.length;
			return coordinateList.length / 2;
		}

		/**
		 * a part is a connected sequence of points. a line is made up of one part
		 */
		public int getNumParts() {
			return 1;
		}

		// the index of the first part in the points array
		public int[] getParts() {
			return parts;
		}

		// the points
		public double[] getPoints() {
			// return points;
			return coordinateList;
		}

		// since there is only one part, the part points are the same as all the
		// points
		public double[] getPartPoints(int part) {
			// return points;
			return coordinateList;
		}

		// the bounding box
		public double[] getBoundingBox() {
			return boundingBox;
		}

		// we will never use this, leave blank
		public boolean resetPoints(double[] points) {
			return true;
		}

		// will never use this, leave blank
		public void write(LEDataOutputStream file) throws java.io.IOException {
		}

		// will never use this, leave blank
		public int getLength() {
			return 0;
		}
	}

	private class Category {
		Category() {
		}

		String	name;
		int		attributeFormatCodes;
		int		highestNodeIDNumber;
		int		actualNumberOfNodes;

		boolean	presenceOfNodeToAreaLinkageRecords;
		boolean	presenceOfNodeToLineLinkageRecords;

		int		highestAreaIDNumber;
		int		actualNumberOfAreas;

		boolean	presenceOfAreaToNodeLinkageRecords;
		boolean	presenceOfAreaToLineLinkageRecords;
		boolean	presenceOfAreaCoordinateLists;

		int		highestLineIDNumber;
		int		actualNumberOfLines;

		boolean	presenceOfLineCoordinateLists;
	}

	private static final int	MAJOR	= 0;
	private static final int	MINOR	= 1;
	private static final int	VALUE	= 2;

	private static double[] reverse(double[] d) {
		int len = d.length;
		double[] retVal = new double[d.length];
		System.arraycopy(d, 0, retVal, 0, d.length);

		// start at the end of d
		// copy each element to retVal

		// NEED TO KEEP COORDINATES TOGETHER IN SAME ORDER
		// THIS WOULD MIX UP X AND Y

		/*
		 * for(int i = len-1, index = 0; i >= 0; i--, index++) { retVal[index] =
		 * d[i]; }
		 */

		int frontIndex = 0;
		int endIndex = d.length - 2;

		while (frontIndex <= endIndex) {
			double frontx = retVal[frontIndex];
			double fronty = retVal[frontIndex + 1];

			retVal[frontIndex] = retVal[endIndex];
			retVal[frontIndex + 1] = retVal[endIndex + 1];

			retVal[endIndex] = frontx;
			retVal[endIndex + 1] = fronty;

			frontIndex += 2;
			endIndex -= 2;
		}

		return retVal;
	} // reverse

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("******* DLG ********");
		sb.append(this.boundaryPoints);

		return sb.toString();
	}
}
