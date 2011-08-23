
#include "ncsa_im2learn_core_geo_projection_MRTInterface.h"
#include <cproj.h>
#include <proj.h>


/*
 * Class:     ncsa_im2learn_core_geo_projection_MRTInterface
 * Method:    geo2coord
 * Signature: (IDD[D)J
 */
JNIEXPORT jlong JNICALL Java_ncsa_im2learn_core_geo_projection_MRTInterface_geo2coord
  (JNIEnv *env, jclass cl, jint proj, jdouble lon, jdouble lat, jdoubleArray xy)
{
	double x, y;
	long ret;
	jdouble *pXY = NULL;

	switch (proj)
	{
	case 0 :
		x = lon;
		y = lat;
		ret = OK;
		break;

	case 1 : // 1 = Universal Transverse Mercator (UTM)
		ret = utmfor(lon, lat, &x, &y);
		break; 

	case 2 : // 2 = State Plane Coordinates
		ret = stplnfor(lon, lat, &x, &y);
		break;

	case 3 : // 3 = Albers Conical Equal Area
		ret = alberfor(lon, lat, &x, &y);
		break;
	 
	case 4 : // 4 = Lambert Conformal Conic
		ret = lamccfor(lon, lat, &x, &y);
		break;

	case 5 : // 5 = Mercator
		ret = merfor(lon, lat, &x, &y);
		break;

	case 6 : // 6 = Polar Stereographic
		ret = psfor(lon, lat, &x, &y);
		break;

	case 7 : // 7 = Polyconic
		ret = polyfor(lon, lat, &x, &y);
		break;

	case 8 : // 8 = Equidistant Conic
		ret = eqconfor(lon, lat, &x, &y);
		break;

	case 9 : // 9 = Transverse Mercator
		ret = tmfor(lon, lat, &x, &y);
		break;

	case 10 : // 10 = Stereographic
		ret = sterfor(lon, lat, &x, &y);
		break;

	case 11 : // 11 = Lambert Azimuthal Equal Area
		ret = lamazfor(lon, lat, &x, &y);
		break;
		
	case 12 : // 12 = Azimuthal Equidistant
		ret = azimfor(lon, lat, &x, &y);
		break;

	case 13 : // 13 = Gnomonic
		ret = gnomfor(lon, lat, &x, &y);
		break;

	case 14 : // 14 = Orthographic
		ret = orthfor(lon, lat, &x, &y);
		break;

	case 15 : // 15 = General Vertical Near-Side Perspective
		ret = gvnspfor(lon, lat, &x, &y);
		break;

	case 16 : // 16 = Sinusiodal
		ret = sinfor(lon, lat, &x, &y);
		break;

	case 17 : // 17 = Equirectangular
		ret = equifor(lon, lat, &x, &y);
		break;

	case 18 : // 18 = Miller Cylindrical
		ret = millfor(lon, lat, &x, &y);
		break;

	case 19 : // 19 = Van der Grinten
		ret = vandgfor(lon, lat, &x, &y);
		break;

	case 20 : // 20 = (Hotine) Oblique Mercator
		ret = omerfor(lon, lat, &x, &y);
		break;
		
	case 21 : // 21 = Robinson
		ret = robfor(lon, lat, &x, &y);
		break;

	case 22 : // 22 = Space Oblique Mercator (SOM)
		ret = somfor(lon, lat, &x, &y);
		break;

	case 23 : // 23 = Alaska Conformal
		ret = alconfor(lon, lat, &x, &y);
		break;

	case 24 : // 24 = Interrupted Goode Homolosine
		ret = goodfor(lon, lat, &x, &y);
		break;

	case 25 : // 25 = Mollweide
		ret = molwfor(lon, lat, &x, &y);
		break;

	case 26 : // 26 = Interrupted Mollweide
		ret = imolwfor(lon, lat, &x, &y);
		break;

	case 27 : // 27 = Hammer
		ret = hamfor(lon, lat, &x, &y);
		break;

	case 28 : // 28 = Wagner IV
		ret = wivfor(lon, lat, &x, &y);
		break;

	case 29 : // 29 = Wagner VII
		ret = wviifor(lon, lat, &x, &y);
		break;

	case 30 : // 30 = Oblated Equal Area
		ret = obleqfor(lon, lat, &x, &y);
		break;

	case 31 : // 31 = Integerized Sinusoidal
		ret = isinusfor(lon, lat, &x, &y);
		break;

	default :
		ret = -1;
	}

	if (ret != OK)
		return -1;
	else
	{
		pXY = (*env)->GetDoubleArrayElements(env, xy, NULL);
		pXY[0] = x;
		pXY[1] = y;
		(*env)->ReleaseDoubleArrayElements(env, xy, pXY, 0);

		return 1;
	}
}
 
/*
 * Class:     ncsa_im2learn_core_geo_projection_MRTInterface
 * Method:    coord2geo
 * Signature: (IDD[D)J
 */
JNIEXPORT jlong JNICALL Java_ncsa_im2learn_core_geo_projection_MRTInterface_coord2geo
  (JNIEnv *env, jclass cl, jint proj, jdouble x, jdouble y, jdoubleArray lonlat)
{
	double lon, lat;
	long ret;
	jdouble *pLonLat = NULL;

	switch (proj)
	{
	case 0 :
		lon = x;
		lat = y;
		ret = OK;
		break;

	case 1 : // 1 = Universal Transverse Mercator (UTM)
		ret = utminv(x, y, &lon, &lat);
		break; 

	case 2 : // 2 = State Plane Coordinates
		ret = stplninv(x, y, &lon, &lat);
		break;

	case 3 : // 3 = Albers Conical Equal Area
		ret = alberinv(x, y, &lon, &lat);
		break;
	 
	case 4 : // 4 = Lambert Coninvmal Conic
		ret = lamccinv(x, y, &lon, &lat);
		break;

	case 5 : // 5 = Mercator
		ret = merinv(x, y, &lon, &lat);
		break;

	case 6 : // 6 = Polar Stereographic
		ret = psinv(x, y, &lon, &lat);
		break;

	case 7 : // 7 = Polyconic
		ret = polyinv(x, y, &lon, &lat);
		break;

	case 8 : // 8 = Equidistant Conic
		ret = eqconinv(x, y, &lon, &lat);
		break;

	case 9 : // 9 = Transverse Mercator
		ret = tminv(x, y, &lon, &lat);
		break;

	case 10 : // 10 = Stereographic
		ret = sterinv(x, y, &lon, &lat);
		break;

	case 11 : // 11 = Lambert Azimuthal Equal Area
		ret = lamazinv(x, y, &lon, &lat);
		break;
		
	case 12 : // 12 = Azimuthal Equidistant
		ret = aziminv(x, y, &lon, &lat);
		break;

	case 13 : // 13 = Gnomonic
		ret = gnominv(x, y, &lon, &lat);
		break;

	case 14 : // 14 = Orthographic
		ret = orthinv(x, y, &lon, &lat);
		break;

	case 15 : // 15 = General Vertical Near-Side Perspective
		ret = gvnspinv(x, y, &lon, &lat);
		break;

	case 16 : // 16 = Sinusiodal
		ret = sininv(x, y, &lon, &lat);
		break;

	case 17 : // 17 = Equirectangular
		ret = equiinv(x, y, &lon, &lat);
		break;

	case 18 : // 18 = Miller Cylindrical
		ret = millinv(x, y, &lon, &lat);
		break;

	case 19 : // 19 = Van der Grinten
		ret = vandginv(x, y, &lon, &lat);
		break;

	case 20 : // 20 = (Hotine) Oblique Mercator
		ret = omerinv(x, y, &lon, &lat);
		break;
		
	case 21 : // 21 = Robinson
		ret = robinv(x, y, &lon, &lat);
		break;

	case 22 : // 22 = Space Oblique Mercator (SOM)
		ret = sominv(x, y, &lon, &lat);
		break;

	case 23 : // 23 = Alaska Coninvmal
		ret = alconinv(x, y, &lon, &lat);
		break;

	case 24 : // 24 = Interrupted Goode Homolosine
		ret = goodinv(x, y, &lon, &lat);
		break;

	case 25 : // 25 = Mollweide
		ret = molwinv(x, y, &lon, &lat);
		break;

	case 26 : // 26 = Interrupted Mollweide
		ret = imolwinv(x, y, &lon, &lat);
		break;

	case 27 : // 27 = Hammer
		ret = haminv(x, y, &lon, &lat);
		break;

	case 28 : // 28 = Wagner IV
		ret = wivinv(x, y, &lon, &lat);
		break;

	case 29 : // 29 = Wagner VII
		ret = wviiinv(x, y, &lon, &lat);
		break;

	case 30 : // 30 = Obyed Equal Area
		ret = obleqinv(x, y, &lon, &lat);
		break;

	case 31 : // 31 = Integerized Sinusoidal
		ret = isinusinv(x, y, &lon, &lat);
		break;

	default :
		ret = -1;
	}

	if (ret != OK)
		return -1;
	else
	{
		pLonLat = (*env)->GetDoubleArrayElements(env, lonlat, NULL);
		pLonLat[0] = lon;
		pLonLat[1] = lat;
		(*env)->ReleaseDoubleArrayElements(env, lonlat, pLonLat, 0);

		return 1;
	}
}

/*
 * Class:     ncsa_im2learn_core_geo_projection_MRTInterface
 * Method:    utmint
 * Signature: (IDDDJ)J
 */
JNIEXPORT jlong JNICALL Java_ncsa_im2learn_core_geo_projection_MRTInterface_utmint
  (JNIEnv *env, jclass cl, jint fwd, jdouble r_maj, jdouble r_min, jdouble scale_fact, jlong zone)
{
	long ret;

	if (fwd == 1)
		ret = utmforint(r_maj, r_min, scale_fact, zone);
	else      
		ret = utminvint(r_maj, r_min, scale_fact, zone);

	if (ret == OK)
		return 1;
	else
		return -1;
}

/*
 * Class:     ncsa_im2learn_core_geo_projection_MRTInterface
 * Method:    stplnint
 * Signature: (IJJLjava/lang/String;Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_ncsa_im2learn_core_geo_projection_MRTInterface_stplnint
  (JNIEnv *env, jclass cl, jint fwd, jlong zone, jlong sphere, jstring fn27, jstring fn83)
{
	long ret;
	const char *cfn27, *cfn83;

	cfn27 = (*env)->GetStringUTFChars(env, fn27, 0);
	cfn83 = (*env)->GetStringUTFChars(env, fn83, 0);

	if (fwd == 1)
		ret = stplnforint(zone, sphere, (char*)cfn27, (char*)cfn83);
	else
		ret = stplninvint(zone, sphere, (char*)cfn27, (char*)cfn83);

	(*env)->ReleaseStringUTFChars(env, fn27, cfn27);
	(*env)->ReleaseStringUTFChars(env, fn83, cfn83);

	if (ret == OK)
		return 1;
	else
		return -1;	
}

/*
 * Class:     ncsa_im2learn_core_geo_projection_MRTInterface
 * Method:    alberint
 * Signature: (IDDDDDDDD)J
 */
JNIEXPORT jlong JNICALL Java_ncsa_im2learn_core_geo_projection_MRTInterface_alberint
  (JNIEnv *env, jclass cl, jint fwd, jdouble r_maj, jdouble r_min, jdouble lat1, jdouble lat2, jdouble lon0, jdouble lat0, jdouble false_east, jdouble false_north)
{
	long ret;

	if (fwd == 1)
		ret = alberforint(r_maj, r_min, lat1, lat2, lon0, lat0, false_east, false_north);
	else
		ret = alberinvint(r_maj, r_min, lat1, lat2, lon0, lat0, false_east, false_north);

	if (ret == OK)
		return 1;
	else
		return -1;	

}

/*
 * Class:     ncsa_im2learn_core_geo_projection_MRTInterface
 * Method:    lamccint
 * Signature: (IDDDDDDDD)J
 */
JNIEXPORT jlong JNICALL Java_ncsa_im2learn_core_geo_projection_MRTInterface_lamccint
  (JNIEnv *env, jclass cl, jint fwd, jdouble r_maj, jdouble r_min, jdouble lat1, jdouble lat2, jdouble c_lon, jdouble c_lat, jdouble false_east, jdouble false_north)
{
	long ret;

	if (fwd == 1)
		ret = lamccforint(r_maj, r_min, lat1, lat2, c_lon, c_lat, false_east, false_north);
	else
		ret = lamccinvint(r_maj, r_min, lat1, lat2, c_lon, c_lat, false_east, false_north);

	if (ret == OK)
		return 1;
	else
		return -1;	

}

/*
 * Class:     ncsa_im2learn_core_geo_projection_MRTInterface
 * Method:    merint
 * Signature: (IDDDDDD)J
 */
JNIEXPORT jlong JNICALL Java_ncsa_im2learn_core_geo_projection_MRTInterface_merint
  (JNIEnv *env, jclass cl, jint fwd, jdouble r_maj, jdouble r_min, jdouble center_lon, jdouble center_lat, jdouble false_east, jdouble false_north)
{
	long ret;

	if (fwd == 1)
		ret = merforint(r_maj, r_min, center_lon, center_lat, false_east, false_north);
	else
		ret = merinvint(r_maj, r_min, center_lon, center_lat, false_east, false_north);

	if (ret == OK)
		return 1;
	else
		return -1;	

}

/*
 * Class:     ncsa_im2learn_core_geo_projection_MRTInterface
 * Method:    psint
 * Signature: (IDDDDDD)J
 */
JNIEXPORT jlong JNICALL Java_ncsa_im2learn_core_geo_projection_MRTInterface_psint
  (JNIEnv *env, jclass cl, jint fwd, jdouble r_maj, jdouble r_min, jdouble c_lon, jdouble c_lat, jdouble false_east, jdouble false_north)
{
	long ret;

	if (fwd == 1)
		ret = psforint(r_maj, r_min, c_lon, c_lat, false_east, false_north);
	else
		ret = psinvint(r_maj, r_min, c_lon, c_lat, false_east, false_north);

	if (ret == OK)
		return 1;
	else
		return -1;	

}

/*
 * Class:     ncsa_im2learn_core_geo_projection_MRTInterface
 * Method:    polyint
 * Signature: (IDDDDDD)J
 */
JNIEXPORT jlong JNICALL Java_ncsa_im2learn_core_geo_projection_MRTInterface_polyint
  (JNIEnv *env, jclass cl, jint fwd, jdouble r_maj, jdouble r_min, jdouble center_lon, jdouble center_lat, jdouble false_east, jdouble false_north)
{
	long ret;

	if (fwd == 1)
		ret = polyforint(r_maj, r_min, center_lon, center_lat, false_east, false_north);
	else
		ret = polyinvint(r_maj, r_min, center_lon, center_lat, false_east, false_north);

	if (ret == OK)
		return 1;
	else
		return -1;	

}

/*
 * Class:     ncsa_im2learn_core_geo_projection_MRTInterface
 * Method:    eqconint
 * Signature: (IDDDDDDDDJ)J
 */
JNIEXPORT jlong JNICALL Java_ncsa_im2learn_core_geo_projection_MRTInterface_eqconint
  (JNIEnv *env, jclass cl, jint fwd, jdouble r_maj, jdouble r_min, jdouble lat1, jdouble lat2, jdouble center_lon, jdouble center_lat, jdouble false_east, jdouble false_north, jlong mode)
{
	long ret;

	if (fwd == 1)
		ret = eqconforint(r_maj, r_min, lat1, lat2, center_lon, center_lat, false_east, false_north, mode);
	else
		ret = eqconinvint(r_maj, r_min, lat1, lat2, center_lon, center_lat, false_east, false_north, mode);

	if (ret == OK)
		return 1;
	else
		return -1;	

}

/*
 * Class:     ncsa_im2learn_core_geo_projection_MRTInterface
 * Method:    tmint
 * Signature: (IDDDDDDD)J
 */
JNIEXPORT jlong JNICALL Java_ncsa_im2learn_core_geo_projection_MRTInterface_tmint
  (JNIEnv *env, jclass cl, jint fwd, jdouble r_maj, jdouble r_min, jdouble scale_fact, jdouble center_lon, jdouble center_lat, jdouble false_east, jdouble false_north)
{
	long ret;

	if (fwd == 1)
		ret = tmforint(r_maj, r_min, scale_fact, center_lon, center_lat, false_east, false_north);
	else
		ret = tminvint(r_maj, r_min, scale_fact, center_lon, center_lat, false_east, false_north);

	if (ret == OK)
		return 1;
	else
		return -1;	

}

/*
 * Class:     ncsa_im2learn_core_geo_projection_MRTInterface
 * Method:    sterint
 * Signature: (IDDDDD)J
 */
JNIEXPORT jlong JNICALL Java_ncsa_im2learn_core_geo_projection_MRTInterface_sterint
  (JNIEnv *env, jclass cl, jint fwd, jdouble r_maj, jdouble center_lon, jdouble center_lat, jdouble false_east, jdouble false_north)
{
	long ret;

	if (fwd == 1)
		ret = sterforint(r_maj, center_lon, center_lat, false_east, false_north);
	else
		ret = sterinvint(r_maj, center_lon, center_lat, false_east, false_north);

	if (ret == OK)
		return 1;
	else
		return -1;	

}

/*
 * Class:     ncsa_im2learn_core_geo_projection_MRTInterface
 * Method:    lamazint
 * Signature: (IDDDDD)J
 */
JNIEXPORT jlong JNICALL Java_ncsa_im2learn_core_geo_projection_MRTInterface_lamazint
  (JNIEnv *env, jclass cl, jint fwd, jdouble r, jdouble center_lon, jdouble center_lat, jdouble false_east, jdouble false_north)
{
	long ret;

	if (fwd == 1)
		ret = lamazforint(r, center_lon, center_lat, false_east, false_north);
	else
		ret = lamazinvint(r, center_lon, center_lat, false_east, false_north);

	if (ret == OK)
		return 1;
	else
		return -1;	

}

/*
 * Class:     ncsa_im2learn_core_geo_projection_MRTInterface
 * Method:    azimint
 * Signature: (IDDDDD)J
 */
JNIEXPORT jlong JNICALL Java_ncsa_im2learn_core_geo_projection_MRTInterface_azimint
  (JNIEnv *env, jclass cl, jint fwd, jdouble r_maj, jdouble center_lon, jdouble center_lat, jdouble false_east, jdouble false_north)
{
	long ret;

	if (fwd == 1)
		ret = azimforint(r_maj, center_lon, center_lat, false_east, false_north);
	else
		ret = aziminvint(r_maj, center_lon, center_lat, false_east, false_north);

	if (ret == OK)
		return 1;
	else
		return -1;	

}

/*
 * Class:     ncsa_im2learn_core_geo_projection_MRTInterface
 * Method:    gnomint
 * Signature: (IDDDDD)J
 */
JNIEXPORT jlong JNICALL Java_ncsa_im2learn_core_geo_projection_MRTInterface_gnomint
  (JNIEnv *env, jclass cl, jint fwd, jdouble r, jdouble center_lon, jdouble center_lat, jdouble false_east, jdouble false_north)
{
	long ret;

	if (fwd == 1)
		ret = gnomforint(r, center_lon, center_lat, false_east, false_north);
	else
		ret = gnominvint(r, center_lon, center_lat, false_east, false_north);

	if (ret == OK)
		return 1;
	else
		return -1;	

}

/*
 * Class:     ncsa_im2learn_core_geo_projection_MRTInterface
 * Method:    orthint
 * Signature: (IDDDDD)J
 */
JNIEXPORT jlong JNICALL Java_ncsa_im2learn_core_geo_projection_MRTInterface_orthint
  (JNIEnv *env, jclass cl, jint fwd, jdouble r_maj, jdouble center_lon, jdouble center_lat, jdouble false_east, jdouble false_north)
{
	long ret;

	if (fwd == 1)
		ret = orthforint(r_maj, center_lon, center_lat, false_east, false_north);
	else
		ret = orthinvint(r_maj, center_lon, center_lat, false_east, false_north);

	if (ret == OK)
		return 1;
	else
		return -1;	

}

/*
 * Class:     ncsa_im2learn_core_geo_projection_MRTInterface
 * Method:    gvnspint
 * Signature: (IDDDDDD)J
 */
JNIEXPORT jlong JNICALL Java_ncsa_im2learn_core_geo_projection_MRTInterface_gvnspint
  (JNIEnv *env, jclass cl, jint fwd, jdouble r, jdouble h, jdouble center_lon, jdouble center_lat, jdouble false_east, jdouble false_north)
{
	long ret;

	if (fwd == 1)
		ret = gvnspforint(r, h, center_lon, center_lat, false_east, false_north);
	else
		ret = gvnspinvint(r, h, center_lon, center_lat, false_east, false_north);

	if (ret == OK)
		return 1;
	else
		return -1;	

}

/*
 * Class:     ncsa_im2learn_core_geo_projection_MRTInterface
 * Method:    sinint
 * Signature: (IDDDD)J
 */
JNIEXPORT jlong JNICALL Java_ncsa_im2learn_core_geo_projection_MRTInterface_sinint
  (JNIEnv *env, jclass cl, jint fwd, jdouble r, jdouble center_long, jdouble false_east, jdouble false_north)
{
	long ret;

	if (fwd == 1)
		ret = sinforint(r, center_long, false_east, false_north);
	else
		ret = sininvint(r, center_long, false_east, false_north);

	if (ret == OK)
		return 1;
	else
		return -1;	

}

/*
 * Class:     ncsa_im2learn_core_geo_projection_MRTInterface
 * Method:    equiint
 * Signature: (IDDDDD)J
 */
JNIEXPORT jlong JNICALL Java_ncsa_im2learn_core_geo_projection_MRTInterface_equiint
  (JNIEnv *env, jclass cl, jint fwd, jdouble r_maj, jdouble center_lon, jdouble lat1, jdouble false_east, jdouble false_north)
{
	long ret;

	if (fwd == 1)
		ret = equiforint(r_maj, center_lon, lat1, false_east, false_north);
	else
		ret = equiinvint(r_maj, center_lon, lat1, false_east, false_north);

	if (ret == OK)
		return 1;
	else
		return -1;	

}

/*
 * Class:     ncsa_im2learn_core_geo_projection_MRTInterface
 * Method:    millint
 * Signature: (IDDDD)J
 */
JNIEXPORT jlong JNICALL Java_ncsa_im2learn_core_geo_projection_MRTInterface_millint
  (JNIEnv *env, jclass cl, jint fwd, jdouble r, jdouble center_long, jdouble false_east, jdouble false_north)
{
	long ret;

	if (fwd == 1)
		ret = millforint(r, center_long, false_east, false_north);
	else
		ret = millinvint(r, center_long, false_east, false_north);

	if (ret == OK)
		return 1;
	else
		return -1;	

}

/*
 * Class:     ncsa_im2learn_core_geo_projection_MRTInterface
 * Method:    vandgint
 * Signature: (IDDDD)J
 */
JNIEXPORT jlong JNICALL Java_ncsa_im2learn_core_geo_projection_MRTInterface_vandgint
  (JNIEnv *env, jclass cl, jint fwd, jdouble r, jdouble center_long, jdouble false_east, jdouble false_north)
{
	long ret;

	if (fwd == 1)
		ret = vandgforint(r, center_long, false_east, false_north);
	else
		ret = vandginvint(r, center_long, false_east, false_north);

	if (ret == OK)
		return 1;
	else
		return -1;	

}

/*
 * Class:     ncsa_im2learn_core_geo_projection_MRTInterface
 * Method:    omerint
 * Signature: (IDDDDDDDDDDDDJ)J
 */
JNIEXPORT jlong JNICALL Java_ncsa_im2learn_core_geo_projection_MRTInterface_omerint
  (JNIEnv *env, jclass cl, jint fwd, jdouble r_maj, jdouble r_min, jdouble scale_fact, jdouble azimuth, jdouble lon_orig, jdouble lat_orig, jdouble false_east, jdouble false_north, jdouble lon1, jdouble lat1, jdouble lon2, jdouble lat2, jlong mode)
{
	long ret;

	if (fwd == 1)
		ret = omerforint(r_maj, r_min, scale_fact, azimuth, lon_orig, lat_orig, false_east, false_north, lon1, lat1, lon2, lat2, mode);
	else
		ret = omerinvint(r_maj, r_min, scale_fact, azimuth, lon_orig, lat_orig, false_east, false_north, lon1, lat1, lon2, lat2, mode);

	if (ret == OK)
		return 1;
	else
		return -1;	

}

/*
 * Class:     ncsa_im2learn_core_geo_projection_MRTInterface
 * Method:    robint
 * Signature: (IDDDD)J
 */
JNIEXPORT jlong JNICALL Java_ncsa_im2learn_core_geo_projection_MRTInterface_robint
  (JNIEnv *env, jclass cl, jint fwd, jdouble r, jdouble center_long, jdouble false_east, jdouble false_north)
{
	long ret;

	if (fwd == 1)
		ret = robforint(r, center_long, false_east, false_north);
	else
		ret = robinvint(r, center_long, false_east, false_north);

	if (ret == OK)
		return 1;
	else
		return -1;	

}

/*
 * Class:     ncsa_im2learn_core_geo_projection_MRTInterface
 * Method:    somint
 * Signature: (IDDJJDDDDDJJ)J
 */
JNIEXPORT jlong JNICALL Java_ncsa_im2learn_core_geo_projection_MRTInterface_somint
  (JNIEnv *env, jclass cl, jint fwd, jdouble r_major, jdouble r_minor, jlong satnum, jlong path, jdouble alf_in, jdouble lon, jdouble false_east, jdouble false_north, jdouble time, jlong start1, jlong flag)
{
	long ret;

	if (fwd == 1)
		ret = somforint(r_major, r_minor, satnum, path, alf_in, lon, false_east, false_north, time, start1, flag);
	else
		ret = sominvint(r_major, r_minor, satnum, path, alf_in, lon, false_east, false_north, time, start1, flag);

	if (ret == OK)
		return 1;
	else
		return -1;	

}

/*
 * Class:     ncsa_im2learn_core_geo_projection_MRTInterface
 * Method:    alconint
 * Signature: (IDDDD)J
 */
JNIEXPORT jlong JNICALL Java_ncsa_im2learn_core_geo_projection_MRTInterface_alconint
  (JNIEnv *env, jclass cl, jint fwd, jdouble r_maj, jdouble r_min, jdouble false_east, jdouble false_north)
{
	long ret;

	if (fwd == 1)
		ret = alconforint(r_maj, r_min, false_east, false_north);
	else
		ret = alconinvint(r_maj, r_min, false_east, false_north);

	if (ret == OK)
		return 1;
	else
		return -1;	

}

/*
 * Class:     ncsa_im2learn_core_geo_projection_MRTInterface
 * Method:    goodint
 * Signature: (ID)J
 */
JNIEXPORT jlong JNICALL Java_ncsa_im2learn_core_geo_projection_MRTInterface_goodint
  (JNIEnv *env, jclass cl, jint fwd, jdouble r)
{
	long ret;

	if (fwd == 1)
		ret = goodforint(r);
	else
		ret = goodinvint(r);

	if (ret == OK)
		return 1;
	else
		return -1;	

}

/*
 * Class:     ncsa_im2learn_core_geo_projection_MRTInterface
 * Method:    molwint
 * Signature: (IDDDD)J
 */
JNIEXPORT jlong JNICALL Java_ncsa_im2learn_core_geo_projection_MRTInterface_molwint
  (JNIEnv *env, jclass cl, jint fwd, jdouble r, jdouble center_long, jdouble false_east, jdouble false_north)
{
	long ret;

	if (fwd == 1)
		ret = molwforint(r, center_long, false_east, false_north);
	else
		ret = molwinvint(r, center_long, false_east, false_north);

	if (ret == OK)
		return 1;
	else
		return -1;	

}

/*
 * Class:     ncsa_im2learn_core_geo_projection_MRTInterface
 * Method:    imolwint
 * Signature: (ID)J
 */
JNIEXPORT jlong JNICALL Java_ncsa_im2learn_core_geo_projection_MRTInterface_imolwint
  (JNIEnv *env, jclass cl, jint fwd, jdouble r)
{
	long ret;

	if (fwd == 1)
		ret = imolwforint(r);
	else
		ret = imolwinvint(r);

	if (ret == OK)
		return 1;
	else
		return -1;	

}

/*
 * Class:     ncsa_im2learn_core_geo_projection_MRTInterface
 * Method:    hamint
 * Signature: (IDDDD)J
 */
JNIEXPORT jlong JNICALL Java_ncsa_im2learn_core_geo_projection_MRTInterface_hamint
  (JNIEnv *env, jclass cl, jint fwd, jdouble r, jdouble center_long, jdouble false_east, jdouble false_north)
{
	long ret;

	if (fwd == 1)
		ret = hamforint(r, center_long, false_east, false_north);
	else
		ret = haminvint(r, center_long, false_east, false_north);

	if (ret == OK)
		return 1;
	else
		return -1;	

}

/*
 * Class:     ncsa_im2learn_core_geo_projection_MRTInterface
 * Method:    wivint
 * Signature: (IDDDD)J
 */
JNIEXPORT jlong JNICALL Java_ncsa_im2learn_core_geo_projection_MRTInterface_wivint
  (JNIEnv *env, jclass cl, jint fwd, jdouble r, jdouble center_long, jdouble false_east, jdouble false_north)
{
	long ret;

	if (fwd == 1)
		ret = wivforint(r, center_long, false_east, false_north);
	else
		ret = wivinvint(r, center_long, false_east, false_north);

	if (ret == OK)
		return 1;
	else
		return -1;	

}

/*
 * Class:     ncsa_im2learn_core_geo_projection_MRTInterface
 * Method:    wviiint
 * Signature: (IDDDD)J
 */
JNIEXPORT jlong JNICALL Java_ncsa_im2learn_core_geo_projection_MRTInterface_wviiint
  (JNIEnv *env, jclass cl, jint fwd, jdouble r, jdouble center_long, jdouble false_east, jdouble false_north)
{
	long ret;

	if (fwd == 1)
		ret = wviiforint(r, center_long, false_east, false_north);
	else
		ret = wviiinvint(r, center_long, false_east, false_north);

	if (ret == OK)
		return 1;
	else
		return -1;	

}

/*
 * Class:     ncsa_im2learn_core_geo_projection_MRTInterface
 * Method:    obleqint
 * Signature: (IDDDDDDDD)J
 */
JNIEXPORT jlong JNICALL Java_ncsa_im2learn_core_geo_projection_MRTInterface_obleqint
  (JNIEnv *env, jclass cl, jint fwd, jdouble r, jdouble center_long, jdouble center_lat, jdouble shape_m, jdouble shape_n, jdouble angle, jdouble false_east, jdouble false_north)
{
	long ret;

	if (fwd == 1)
		ret = obleqforint(r, center_long, center_lat, shape_m, shape_n, angle, false_east, false_north);
	else
		ret = obleqinvint(r, center_long, center_lat, shape_m, shape_n, angle, false_east, false_north);

	if (ret == OK)
		return 1;
	else
		return -1;	

}

/*
 * Class:     ncsa_im2learn_core_geo_projection_MRTInterface
 * Method:    isinusint
 * Signature: (IDDDDDD)J
 */
JNIEXPORT jlong JNICALL Java_ncsa_im2learn_core_geo_projection_MRTInterface_isinusint
  (JNIEnv *env, jclass cl, jint fwd, jdouble sphere, jdouble lon_cen_mer, jdouble false_east, jdouble false_north, jdouble dzone, jdouble djustify)
{
	long ret;

	if (fwd == 1)
		ret = isinusforinit(sphere, lon_cen_mer, false_east, false_north, dzone, djustify);
	else
		ret = isinusinvinit(sphere, lon_cen_mer, false_east, false_north, dzone, djustify);

	if (ret == OK)
		return 1;
	else
		return -1;	

}


