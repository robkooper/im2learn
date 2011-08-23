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
package edu.illinois.ncsa.isda.imagetools.core.io.tiff;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.geo.projection.Projection;

/**
 * Detect the presence of a .tfw file/url. If present, read it in and use its
 * contents to update projection information that came with the .tiff file.
 * Major assumptions (partly justified): - values in the .tfw file must come on
 * separate lines. - the first value must start on the first line.
 * 
 * @author Yakov Keselman
 * @version June 27, 2006.
 */
public class TFWLoader {

	/**
	 * The logger.
	 */
	static protected Log	logger	= LogFactory.getLog(TFWLoader.class);

	private final double	columnResolution;

	private final double	horizontalRotation;

	private final double	verticalRotation;

	private final double	rowResolution;

	private final double	easting;

	private final double	northing;

	/**
	 * @param filename
	 *            the name of the .tiff file.
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public TFWLoader(String filename) throws FileNotFoundException, IOException {
		int idx = filename.lastIndexOf(".");
		BufferedReader br = new java.io.BufferedReader(new FileReader(filename.substring(0, idx) + ".tfw"));

		// Column (horizontal) resolution.
		columnResolution = Double.parseDouble(br.readLine());

		// Rotation in the horizontal direction (not supported/used as of
		// 06/26/2006).
		horizontalRotation = Double.parseDouble(br.readLine());

		// Rotation in the vertical direction (not supported/used as of
		// 06/26/2006).
		verticalRotation = Double.parseDouble(br.readLine());

		// Row (vertical) resolution.
		rowResolution = Double.parseDouble(br.readLine());

		// Easting value of the tie point.
		easting = Double.parseDouble(br.readLine());

		// Northing value of the tie point.
		northing = Double.parseDouble(br.readLine());
	}

	/**
	 * Parse the .tfw file, extracting the 6 values stored there. Update
	 * projection information.
	 * 
	 * @param projection
	 *            to update with new parameters.
	 */
	public void updateProjection(Projection projection) {
		// update projection parameters.
		projection.setScaleX(columnResolution);
		projection.setInsertionX(easting);
		projection.setInsertionY(northing);

		// continue updating projection's parameters.
		if (rowResolution < 0) {
			// negative value of row resolution indicates (0, 0) tie point.
			projection.setRasterSpaceI(0);
			projection.setRasterSpaceJ(0);
			projection.setScaleY(rowResolution);
		} else {
			// positive value of row resolution indicates (0, number_of_rows)
			// tie point.
			projection.setRasterSpaceI(0);
			projection.setRasterSpaceJ(projection.getNumRows());
			projection.setScaleY(rowResolution);
		}

	}

}
