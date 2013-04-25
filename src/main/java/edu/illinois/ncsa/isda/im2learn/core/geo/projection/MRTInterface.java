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

/* Projection codes
   0 = Geographic
   1 = Universal Transverse Mercator (UTM)
   2 = State Plane Coordinates
   3 = Albers Conical Equal Area
   4 = Lambert Conformal Conic
   5 = Mercator
   6 = Polar Stereographic
   7 = Polyconic
   8 = Equidistant Conic
   9 = Transverse Mercator
  10 = Stereographic
  11 = Lambert Azimuthal Equal Area
  12 = Azimuthal Equidistant
  13 = Gnomonic
  14 = Orthographic
  15 = General Vertical Near-Side Perspective
  16 = Sinusiodal
  17 = Equirectangular
  18 = Miller Cylindrical
  19 = Van der Grinten
  20 = (Hotine) Oblique Mercator 
  21 = Robinson
  22 = Space Oblique Mercator (SOM)
  23 = Alaska Conformal
  24 = Interrupted Goode Homolosine 
  25 = Mollweide
  26 = Interrupted Mollweide
  27 = Hammer
  28 = Wagner IV
  29 = Wagner VII
  30 = Oblated Equal Area
  31 = Integerized Sinusoidal
*/

public class MRTInterface {
	
	
	public static final int forward = 1;
	public static final int inverse = 0;
	
	public static native long geo2coord( 
			int proj,	/* projection type						*/
			double lon,	/* (I) Longitude                		*/
			double lat,	/* (I) Latitude                 		*/
			double[] xy /* (O) X and Y projection coordinates  	*/
    );
 
	public static native long coord2geo( 
			int proj,		/* projection type				*/
			double x,		/* (I) X projection coordinate	*/
			double y,		/* (I) Y projection coordinate	*/
			double[] lonlat	/* (O) Longitude and Latitude	*/
    );
	
	// 1 = Universal Transverse Mercator (UTM)
	public static native long utmint( 
			int fwd,			/* forward(1) or inverse(0)	*/
			double r_maj,		/* major axis 				*/
			double r_min,		/* minor axis 				*/
			double scale_fact,	/* scale factor				*/
			long zone			/* zone number 				*/
	);

	// 2 = State Plane Coordinates
	public static native long stplnint( 
			int fwd,		/* forward(1) or inverse(0)						*/
			long zone,		/* zone number 									*/
			long sphere,	/* spheroid number 								*/
			String fn27,	/* name of file containing the NAD27 parameters */
			String fn83		/* name of file containing the NAD83 parameters */
    );

	// 3 = Albers Conical Equal Area
	public static native long alberint(
			int fwd,			/* forward(1) or inverse(0)	*/
			double r_maj, 		/* major axis       		*/
			double r_min,		/* minor axis             	*/
			double lat1,		/* first standard parallel	*/
			double lat2, 		/* second standard parallel	*/
			double lon0, 		/* center longitude        	*/
			double lat0, 		/* center lattitude        	*/
			double false_east,  /* x offset in meters      	*/
			double false_north	/* y offset in meters     	*/
	);
	
	// 4 = Lambert Conformal Conic
	public static native long lamccint( 
			int fwd,			/* forward(1) or inverse(0)	*/
			double r_maj,		/* major axis               */
			double r_min,		/* minor axis               */
			double lat1,		/* first standard parallel  */
			double lat2,		/* second standard parallel */
			double c_lon,		/* center longitude         */
			double c_lat,		/* center latitude          */
			double false_east,	/* x offset in meters       */
			double false_north	/* y offset in meters       */
	);

	// 5 = Mercator
	public static native long merint( 
			int fwd,			/* forward(1) or inverse(0)	*/
			double r_maj,		/* major axis               */
			double r_min,		/* minor axis               */
			double center_lon,	/* center longitude         */
			double center_lat,	/* center latitude          */
			double false_east,	/* x offset in meters       */
			double false_north	/* y offset in meters       */
	);

	// 6 = Polar Stereographic
	public static native long psint( 
			int fwd,			/* forward(1) or inverse(0)	*/
			double r_maj,		/* major axis               */
			double r_min,		/* minor axis               */
			double c_lon,		/* center longitude         */
			double c_lat,		/* center latitude          */
			double false_east,	/* x offset in meters       */
			double false_north	/* y offset in meters       */
	);

	// 7 = Polyconic
	public static native long polyint( 
			int fwd,			/* forward(1) or inverse(0)	*/
			double r_maj,		/* major axis               */
			double r_min,		/* minor axis               */
			double center_lon,	/* center longitude         */
			double center_lat,	/* center latitude          */
			double false_east,	/* x offset in meters       */
			double false_north	/* y offset in meters       */
	);
	
	// 8 = Equidistant Conic
	public static native long eqconint( 
			int fwd,			/* forward(1) or inverse(0)		*/
			double r_maj,		/* major axis                   */
			double r_min,		/* minor axis                   */
			double lat1,		/* latitude of standard parallel*/
			double lat2,		/* latitude of standard parallel*/
			double center_lon,	/* center longitude             */
			double center_lat,	/* center latitude              */
			double false_east,	/* x offset in meters           */
			double false_north,	/* y offset in meters           */
			long mode			/* which format is present A B  */
	);

	// 9 = Transverse Mercator
	public static native long tmint( 
			int fwd,			/* forward(1) or inverse(0)	*/
			double r_maj,		/* major axis               */
			double r_min,		/* minor axis               */
			double scale_fact,	/* scale factor             */
			double center_lon,	/* center longitude         */
			double center_lat,	/* center latitude          */
			double false_east,	/* x offset in meters       */
			double false_north	/* y offset in meters       */
    );

	// 10 = Stereographic
	public static native long sterint( 
			int fwd,			/* forward(1) or inverse(0)	*/
			double r_maj,		/* major axis               */
			double center_lon,	/* center longitude         */
			double center_lat,	/* center latitude          */
			double false_east,	/* x offset in meters       */
			double false_north	/* y offset in meters       */
    );

	// 11 = Lambert Azimuthal Equal Area
	public static native long lamazint( 
			int fwd,			/* forward(1) or inverse(0)			*/
			double r,			/* (I) Radius of the earth (sphere) */
			double center_long,	/* (I) Center longitude             */
			double center_lat,	/* (I) Center latitude              */
			double false_east,	/* x offset in meters               */
			double false_north	/* y offset in meters               */
	);

	// 12 = Azimuthal Equidistant
	public static native long azimint( 
			int fwd,			/* forward(1) or inverse(0)		*/
			double r_maj,		/* major axis                   */
			double center_lon,	/* center longitude             */
			double center_lat,	/* center latitude              */
			double false_east,	/* x offset in meters           */
			double false_north	/* y offset in meters           */
     );

	// 13 = Gnomonic
	public static native long gnomint( 
			int fwd,			/* forward(1) or inverse(0)				*/
			double r,			/* (I) Radius of the earth (sphere)     */
			double center_long,	/* (I) Center longitude                 */
			double center_lat,	/* (I) Center latitude                  */
			double false_east,	/* x offset in meters                   */
			double false_north	/* y offset in meters                   */
    );

	// 14 = Orthographic
	public static native long orthint( 
			int fwd,			/* forward(1) or inverse(0)		*/
			double r_maj,		/* major axis                   */
			double center_lon,	/* center longitude             */
			double center_lat,	/* center latitude              */
			double false_east,	/* x offset in meters           */
			double false_north	/* y offset in meters           */
	);

	// 15 = General Vertical Near-Side Perspective
	public static native long gvnspint( 
			int fwd,			/* forward(1) or inverse(0)				*/
			double r,			/* (I) Radius of the earth (sphere)     */
			double h,			/* height above sphere                  */
			double center_long,	/* (I) Center longitude                 */
			double center_lat,	/* (I) Center latitude                  */
			double false_east,	/* x offset in meters                   */
			double false_north	/* y offset in meters                   */
    );

	// 16 = Sinusiodal
	public static native long sinint( 
			int fwd,			/* forward(1) or inverse(0)				*/
			double r,			/* (I) Radius of the earth (sphere)     */
			double center_long,	/* (I) Center longitude                 */
			double false_east,	/* x offset in meters                   */
			double false_north	/* y offset in meters                   */
    );

	// 17 = Equirectangular
	public static native long equiint( 
			int fwd,			/* forward(1) or inverse(0)		*/
			double r_maj,		/* major axis                   */
			double center_lon,	/* center longitude             */
			double lat1,		/* latitude of true scale       */
			double false_east,	/* x offset in meters           */
			double false_north	/* y offset in meters           */
	);

	// 18 = Miller Cylindrical
	public static native long millint( 
			int fwd,			/* forward(1) or inverse(0)				*/
			double r,			/* (I) Radius of the earth (sphere)     */
			double center_long,	/* (I) Center longitude                 */
			double false_east,	/* x offset in meters                   */
			double false_north	/* y offset in meters                   */
	);

	// 19 = Van der Grinten
	public static native long vandgint( 
			int fwd,			/* forward(1) or inverse(0)				*/
			double r,			/* (I) Radius of the earth (sphere)     */
			double center_long,	/* (I) Center longitude                 */
			double false_east,	/* x offset in meters                   */
			double false_north	/* y offset in meters                   */
     );

	// 20 = (Hotine) Oblique Mercator 
	public static native long omerint( 
			int fwd,			/* forward(1) or inverse(0)	*/
			double r_maj,		/* major axis                   */
			double r_min,		/* minor axis                   */
			double scale_fact,	/* scale factor                 */
			double azimuth,		/* azimuth east of north        */
			double lon_orig,	/* longitude of origin          */
			double lat_orig,	/* center latitude              */
			double false_east,	/* x offset in meters           */
			double false_north,	/* y offset in meters           */
			double lon1,		/* fist point to define central line    */
			double lat1,		/* fist point to define central line    */
			double lon2,		/* second point to define central line  */
			double lat2,		/* second point to define central line  */
			long mode			/* which format type A or B     */
	);

	// 21 = Robinson
	public static native long robint( 
			int fwd,			/* forward(1) or inverse(0)				*/
			double r,			/* (I) Radius of the earth (sphere)     */
			double center_long,	/* (I) Center longitude                 */
			double false_east,	/* x offset in meters                   */
			double false_north	/* y offset in meters                   */
    );

	// 22 = Space Oblique Mercator (SOM)
	public static native long somint( 
			int fwd,			/* forward(1) or inverse(0)				*/
			double r_major,		/* major axis                           */
			double r_minor,		/* minor axis                           */
			long satnum,		/* Landsat satellite number (1,2,3,4,5) */
			long path,			/* Landsat path number 					*/
			double alf_in, 
			double lon, 
			double false_east,	/* x offset in meters                   */
			double false_north,	/* y offset in meters                   */
			double time, 
			long start1, 
			long flag 
	);

	// 23 = Alaska Conformal
	public static native long alconint( 
			int fwd,			/* forward(1) or inverse(0)				*/
			double r_maj,		/* Major axis                           */
			double r_min,		/* Minor axis                           */
			double false_east,	/* x offset in meters                   */
			double false_north	/* y offset in meters                   */
    );

	// 24 = Interrupted Goode Homolosine
	public static native long goodint( 
			int fwd,	/* forward(1) or inverse(0)			*/
			double r	/* (I) Radius of the earth (sphere) */
    );

	// 25 = Mollweide
	public static native long molwint( 
			int fwd,			/* forward(1) or inverse(0)				*/
			double r,			/* (I) Radius of the earth (sphere)     */
			double center_long,	/* (I) Center longitude                 */
			double false_east,	/* x offset in meters                   */
			double false_north	/* y offset in meters                   */
	);

	// 26 = Interrupted Mollweide
	public static native long imolwint( 
			int fwd,	/* forward(1) or inverse(0)			*/
			double r	/* (I) Radius of the earth (sphere) */
    );

	// 27 = Hammer
	public static native long hamint( 
			int fwd,			/* forward(1) or inverse(0)				*/
			double r,			/* (I) Radius of the earth (sphere)     */
			double center_long,	/* (I) Center longitude                 */
			double false_east,	/* x offset in meters                   */
			double false_north	/* y offset in meters                   */
    );

	// 28 = Wagner IV
	public static native long wivint( 
			int fwd,			/* forward(1) or inverse(0)			*/
			double r,			/* (I) Radius of the earth (sphere) */
			double center_long,	/* (I) Center longitude 			*/
			double false_east,	/* x offset                         */
			double false_north	/* y offset                         */
     );

	// 29 = Wagner VII
	public static native long wviiint( 
			int fwd,			/* forward(1) or inverse(0)			*/
			double r,			/* (I) Radius of the earth (sphere) */
			double center_long,	/* (I) Center longitude 			*/
			double false_east,	/* x offset                         */
			double false_north	/* y offset                         */
     );

	// 30 = Oblated Equal Area
	public static native long obleqint( 
			int fwd,			/* forward(1) or inverse(0)	*/
			double r,
			double center_long,
			double center_lat,
			double shape_m,
			double shape_n, 
			double angle, 
			double false_east, 
			double false_north 
	);

	// 31 = Integerized Sinusoidal
	public static native long isinusint( 
			int fwd,			/* forward(1) or inverse(0)	*/
			double sphere,		/* (I) Radius of the earth (sphere) */
		    double lon_cen_mer,	/* (I) Longitude of central meridian (radians) */
		    double false_east,	/* (I) Easting at projection origin (meters) */
		    double false_north,	/* (I) Northing at projection origin (meters) */
		    double dzone,		/* (I) Number of longitudinal zones */
		    double djustify		/* (I) Justify (flag for rows w/odd # of columns) */
     );


	
	// Load DLL for JNI 
	// The path of MRTInterface.dll/MRTInterface.so file has to be 
	// included in the environment %path% variable.
	static{
		System.loadLibrary("MRTInterface");
	}
}


