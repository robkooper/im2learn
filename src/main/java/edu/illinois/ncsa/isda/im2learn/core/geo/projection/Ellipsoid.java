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
package edu.illinois.ncsa.isda.im2learn.core.geo.projection;

import edu.illinois.ncsa.isda.im2learn.core.geo.*;

/**
 * Get the constants for the Ellipsoid.  The supported ellipsoids are defined
 * as constant values.
 */
public final class Ellipsoid {

  /**
   * these constants are defined in section 6.3.2.3 of GeoTIFF spec
   */
  public static final int WGS_84 = 7030;
  public static final int GRS_1980 = 7019;
  public static final int AIRY_1830 = 7001;
  public static final int CLARKE_1866 = 7008;
  public static final int SPHERE = 7035;

  /**
   * Get the major axis, the equatorial radius a.
   * @param type
   * @return
   * @throws GeoException
   */
  public static final double getMajorAxis(int type) throws GeoException {
    switch(type) {
      case WGS_84:
        return 6378137;
      case AIRY_1830:
        return 6377563.396;
      case GRS_1980:
        return 6378135;
      case CLARKE_1866:
        return 6378206.4;
      default:
        throw new GeoException("Unsupported Ellipsoid.");
    }
  }

  /**
   * Get the minor axis, the polar axis b.
   * @param type
   * @return
   * @throws GeoException
   */
  public static final double getMinorAxis(int type) throws GeoException {
    switch(type) {
      case WGS_84:
        return 6356752.3;
      case AIRY_1830:
        return 6356256.910;
      case GRS_1980:
        return 6356752.3;
      case CLARKE_1866:
        return 6356583.8;
      default:
        throw new GeoException("Unsupported Ellipsoid.");
    }
  }

  /**
   * Get the inverse flattening.
   * @param type
   * @return
   * @throws GeoException
   */
  public static final double getInverseFlattening(int type) throws GeoException {
    switch(type) {
      case WGS_84:
        return (1/298.257);
      case AIRY_1830:
        return (1/299.32);
      case GRS_1980:
        return (1/298.257);
      case CLARKE_1866:
        return (1/294.98);
      default:
        throw new GeoException("Unsupported Ellipsoid.");
    }
  }

  /**
   * Get the name of the ellipsoid as a string.
   * @param type
   * @return
   * @throws GeoException
   */
  public static final String getName(int type) throws GeoException {
    switch(type) {
      case WGS_84:
        return "WGS 84";
      case AIRY_1830:
        return "Airy 1830";
      case GRS_1980:
        return "GRS 1980";
      case CLARKE_1866:
        return "Clarke 1866";
      default:
        throw new GeoException("Unsupported Ellipsoid.");
    }
  }

}
