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
package edu.illinois.ncsa.isda.im2learn.core.io.tiff;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.geo.AngularUnit;
import edu.illinois.ncsa.isda.im2learn.core.geo.Datum;
import edu.illinois.ncsa.isda.im2learn.core.geo.Ellipsoid;
import edu.illinois.ncsa.isda.im2learn.core.geo.GeoException;
import edu.illinois.ncsa.isda.im2learn.core.geo.GeoGraphicCoordinateSystem;
import edu.illinois.ncsa.isda.im2learn.core.geo.LinearUnit;
import edu.illinois.ncsa.isda.im2learn.core.geo.ModelPoint;
import edu.illinois.ncsa.isda.im2learn.core.geo.PrimeMeridian;
import edu.illinois.ncsa.isda.im2learn.core.geo.Projection;
import edu.illinois.ncsa.isda.im2learn.core.geo.RasterPoint;
import edu.illinois.ncsa.isda.im2learn.core.geo.TiePoint;
import edu.illinois.ncsa.isda.im2learn.core.geo.Projection.ProjectionType;
import edu.illinois.ncsa.isda.im2learn.core.geo.java.TransverseMercator;

/**
 * Can parse the geotiff information and create a geoinformation from the values
 * provided. Currently this only supports WGS84 / UTM northern hemisphere tags.
 */
public class GeoEntry {
	// GeoTIFF Configuration GeoKeys
	static final int		GTModelTypeGeoKey					= 1024;
	static final int		GTRasterTypeGeoKey					= 1025;
	static final int		GTCitationGeoKey					= 1026;

	// Geographic CS Parameter GeoKeys
	// used if modeltype is userdefined or geographic
	static final int		GeographicTypeGeoKey				= 2048;
	static final int		GeogCitationGeoKey					= 2049;
	static final int		GeogGeodeticDatumGeoKey				= 2050;
	static final int		GeogPrimeMeridianGeoKey				= 2051;
	static final int		GeogLinearUnitsGeoKey				= 2052;
	static final int		GeogLinearUnitSizeGeoKey			= 2053;
	static final int		GeogAngularUnitsGeoKey				= 2054;
	static final int		GeogAngularUnitSizeGeoKey			= 2055;
	static final int		GeogEllipsoidGeoKey					= 2056;
	static final int		GeogSemiMajorAxisGeoKey				= 2057;
	static final int		GeogSemiMinorAxisGeoKey				= 2058;
	static final int		GeogInvFlatteningGeoKey				= 2059;
	static final int		GeogAzimuthUnitsGeoKey				= 2060;
	static final int		GeogPrimeMeridianLongGeoKey			= 2061;

	// Projected CS Parameter GeoKeys
	// used if modeltype is projected
	static final int		ProjectedCSTypeGeoKey				= 3072;
	static final int		PCSCitationGeoKey					= 3073;
	static final int		ProjectionGeoKey					= 3074;
	static final int		ProjCoordTransGeoKey				= 3075;
	static final int		ProjLinearUnitsGeoKey				= 3076;
	static final int		ProjLinearUnitSizeGeoKey			= 3077;
	static final int		ProjStdParallel1GeoKey				= 3078;
	static final int		ProjStdParallel2GeoKey				= 3079;
	static final int		ProjNatOriginLongGeoKey				= 3080;
	static final int		ProjNatOriginLatGeoKey				= 3081;
	static final int		ProjFalseEastingGeoKey				= 3082;
	static final int		ProjFalseNorthingGeoKey				= 3083;
	static final int		ProjFalseOriginLongGeoKey			= 3084;
	static final int		ProjFalseOriginLatGeoKey			= 3085;
	static final int		ProjFalseOriginEastingGeoKey		= 3086;
	static final int		ProjFalseOriginNorthingGeoKey		= 3087;
	static final int		ProjCenterLongGeoKey				= 3088;
	static final int		ProjCenterLatGeoKey					= 3089;
	static final int		ProjCenterEastingGeoKey				= 3090;
	static final int		ProjCenterNorthingGeoKey			= 3091;
	static final int		ProjScaleAtNatOriginGeoKey			= 3092;
	static final int		ProjScaleAtCenterGeoKey				= 3093;
	static final int		ProjAzimuthAngleGeoKey				= 3094;
	static final int		ProjStraightVertPoleLongGeoKey		= 3095;

	// Vertical CS Keys
	static final int		VerticalCSTypeGeoKey				= 4096;
	static final int		VerticalCitationGeoKey				= 4097;
	static final int		VerticalDatumGeoKey					= 4098;
	static final int		VerticalUnitsGeoKey					= 4099;

	// user defined
	static final int		UserDefined							= 32767;

	// model types
	/**
	 * Model type codes taken from section 6.3.1.1
	 */
	static final int		ModelTypeProjected					= 1;
	static final int		ModelTypeGeographic					= 2;
	static final int		ModelTypeGeocentric					= 3;

	// raster types
	static final int		RasterPixelIsArea					= 1;
	static final int		RasterPixelIsPoint					= 2;

	/*
	 * Coordinate Transformation Value codes taken from section 6.3.3.3 of
	 * GeoTIFF spec
	 */
	// static final int CT_LambertAzimEqualArea = 10;
	// static final int CT_AlbersEqualArea = 11;
	// static final int CT_Sinusoidal = 24;
	static final int		CT_TransverseMercator				= 1;
	static final int		CT_TransvMercator_Modified_Alaska	= 2;
	static final int		CT_ObliqueMercator					= 3;
	static final int		CT_ObliqueMercator_Laborde			= 4;
	static final int		CT_ObliqueMercator_Rosenmund		= 5;
	static final int		CT_ObliqueMercator_Spherical		= 6;
	static final int		CT_Mercator							= 7;
	static final int		CT_LambertConfConic_2SP				= 8;
	static final int		CT_LambertConfConic_1SP				= 9;
	static final int		CT_LambertAzimEqualArea				= 10;
	static final int		CT_AlbersEqualArea					= 11;
	static final int		CT_AzimuthalEquidistant				= 12;
	static final int		CT_EquidistantConic					= 13;
	static final int		CT_Stereographic					= 14;
	static final int		CT_PolarStereographic				= 15;
	static final int		CT_ObliqueStereographic				= 16;
	static final int		CT_Equirectangular					= 17;
	static final int		CT_CassiniSoldner					= 18;
	static final int		CT_Gnomonic							= 19;
	static final int		CT_MillerCylindrical				= 20;
	static final int		CT_Orthographic						= 21;
	static final int		CT_Polyconic						= 22;
	static final int		CT_Robinson							= 23;
	static final int		CT_Sinusoidal						= 24;
	static final int		CT_VanDerGrinten					= 25;
	static final int		CT_NewZealandMapGrid				= 26;
	static final int		CT_TransvMercator_SouthOriented		= 27;
	// Aliases:
	static final int		CT_AlaskaConformal					= CT_TransvMercator_Modified_Alaska;
	static final int		CT_TransvEquidistCylindrical		= CT_CassiniSoldner;
	static final int		CT_ObliqueMercator_Hotine			= CT_ObliqueMercator;
	static final int		CT_SwissObliqueCylindrical			= CT_ObliqueMercator_Rosenmund;
	static final int		CT_GaussBoaga						= CT_TransverseMercator;
	static final int		CT_GaussKruger						= CT_TransverseMercator;
	static final int		CT_LambertConfConic					= CT_LambertConfConic_2SP;
	static final int		CT_LambertConfConic_Helmert			= CT_LambertConfConic_1SP;
	static final int		CT_SouthOrientedGaussConformal		= CT_TransvMercator_SouthOriented;

	/** end coordinate transformation value codes */

	/**
	 * Geodetic Datum Codes taken from secion 6.3.2.2
	 */
	static final int		Datum_North_American_Datum_1983		= 6269;
	static final int		Datum_WGS84							= 6326;

	/**
	 * Ellipsoid codes taken from section 6.3.2.3
	 */
	// static final int Ellipse_GRS_1980 = 7019;
	/**
	 * Linear units codes take from section 6.3.1.3
	 */
	public static final int	Linear_Meter						= 9001;
	public static final int	Linear_Foot							= 9002;
	public static final int	Linear_Foot_US_Survey				= 9003;
	public static final int	Linear_Foot_Modified_American		= 9004;
	public static final int	Linear_Foot_Clarke					= 9005;
	public static final int	Linear_Foot_Indian					= 9006;
	public static final int	Linear_Link							= 9007;
	public static final int	Linear_Link_Benoit					= 9008;
	public static final int	Linear_Link_Sears					= 9009;
	public static final int	Linear_Chain_Benoit					= 9010;
	public static final int	Linear_Chain_Sears					= 9011;
	public static final int	Linear_Yard_Sears					= 9012;
	public static final int	Linear_Yard_Indian					= 9013;
	public static final int	Linear_Fathom						= 9014;
	public static final int	Linear_Mile_International_Nautical	= 9015;

	/**
	 * Angular units codes take from section 6.3.1.4
	 */
	public static final int	Angular_Radian						= 9101;
	public static final int	Angular_Degree						= 9102;
	public static final int	Angular_Arc_Minute					= 9103;
	public static final int	Angular_Arc_Second					= 9104;
	public static final int	Angular_Grad						= 9105;
	public static final int	Angular_Gon							= 9106;
	public static final int	Angular_DMS							= 9107;
	public static final int	Angular_DMS_Hemisphere				= 9108;

	/*
	 * 6.3.3.1 Projected CS Type Codes
	 * http://www.remotesensing.org/geotiff/spec/geotiff6.html#6.3.3.1
	 */
	static public final int	PCS_NAD83_UTM_zone_3N				= 26903;
	static public final int	PCS_NAD83_UTM_zone_4N				= 26904;
	static public final int	PCS_NAD83_UTM_zone_5N				= 26905;
	static public final int	PCS_NAD83_UTM_zone_6N				= 26906;
	static public final int	PCS_NAD83_UTM_zone_7N				= 26907;
	static public final int	PCS_NAD83_UTM_zone_8N				= 26908;
	static public final int	PCS_NAD83_UTM_zone_9N				= 26909;
	static public final int	PCS_NAD83_UTM_zone_10N				= 26910;
	static public final int	PCS_NAD83_UTM_zone_11N				= 26911;
	static public final int	PCS_NAD83_UTM_zone_12N				= 26912;
	static public final int	PCS_NAD83_UTM_zone_13N				= 26913;
	static public final int	PCS_NAD83_UTM_zone_14N				= 26914;
	static public final int	PCS_NAD83_UTM_zone_15N				= 26915;
	static public final int	PCS_NAD83_UTM_zone_16N				= 26916;
	static public final int	PCS_NAD83_UTM_zone_17N				= 26917;
	static public final int	PCS_NAD83_UTM_zone_18N				= 26918;
	static public final int	PCS_NAD83_UTM_zone_19N				= 26919;
	static public final int	PCS_NAD83_UTM_zone_20N				= 26920;
	static public final int	PCS_NAD83_UTM_zone_21N				= 26921;
	static public final int	PCS_NAD83_UTM_zone_22N				= 26922;
	static public final int	PCS_NAD83_UTM_zone_23N				= 26923;

	/*
	 * 6.3.2.1 Geographic CS Type Codes
	 */
	public static final int	GCS_Adindan							= 4201;
	public static final int	GCS_AGD66							= 4202;
	public static final int	GCS_AGD84							= 4203;
	public static final int	GCS_Ain_el_Abd						= 4204;
	public static final int	GCS_Afgooye							= 4205;
	public static final int	GCS_Agadez							= 4206;
	public static final int	GCS_Lisbon							= 4207;
	public static final int	GCS_Aratu							= 4208;
	public static final int	GCS_Arc_1950						= 4209;
	public static final int	GCS_Arc_1960						= 4210;
	public static final int	GCS_Batavia							= 4211;
	public static final int	GCS_Barbados						= 4212;
	public static final int	GCS_Beduaram						= 4213;
	public static final int	GCS_Beijing_1954					= 4214;
	public static final int	GCS_Belge_1950						= 4215;
	public static final int	GCS_Bermuda_1957					= 4216;
	public static final int	GCS_Bern_1898						= 4217;
	public static final int	GCS_Bogota							= 4218;
	public static final int	GCS_Bukit_Rimpah					= 4219;
	public static final int	GCS_Camacupa						= 4220;
	public static final int	GCS_Campo_Inchauspe					= 4221;
	public static final int	GCS_Cape							= 4222;
	public static final int	GCS_Carthage						= 4223;
	public static final int	GCS_Chua							= 4224;
	public static final int	GCS_Corrego_Alegre					= 4225;
	public static final int	GCS_Cote_d_Ivoire					= 4226;
	public static final int	GCS_Deir_ez_Zor						= 4227;
	public static final int	GCS_Douala							= 4228;
	public static final int	GCS_Egypt_1907						= 4229;
	public static final int	GCS_ED50							= 4230;
	public static final int	GCS_ED87							= 4231;
	public static final int	GCS_Fahud							= 4232;
	public static final int	GCS_Gandajika_1970					= 4233;
	public static final int	GCS_Garoua							= 4234;
	public static final int	GCS_Guyane_Francaise				= 4235;
	public static final int	GCS_Hu_Tzu_Shan						= 4236;
	public static final int	GCS_HD72							= 4237;
	public static final int	GCS_ID74							= 4238;
	public static final int	GCS_Indian_1954						= 4239;
	public static final int	GCS_Indian_1975						= 4240;
	public static final int	GCS_Jamaica_1875					= 4241;
	public static final int	GCS_JAD69							= 4242;
	public static final int	GCS_Kalianpur						= 4243;
	public static final int	GCS_Kandawala						= 4244;
	public static final int	GCS_Kertau							= 4245;
	public static final int	GCS_KOC								= 4246;
	public static final int	GCS_La_Canoa						= 4247;
	public static final int	GCS_PSAD56							= 4248;
	public static final int	GCS_Lake							= 4249;
	public static final int	GCS_Leigon							= 4250;
	public static final int	GCS_Liberia_1964					= 4251;
	public static final int	GCS_Lome							= 4252;
	public static final int	GCS_Luzon_1911						= 4253;
	public static final int	GCS_Hito_XVIII_1963					= 4254;
	public static final int	GCS_Herat_North						= 4255;
	public static final int	GCS_Mahe_1971						= 4256;
	public static final int	GCS_Makassar						= 4257;
	public static final int	GCS_EUREF89							= 4258;
	public static final int	GCS_Malongo_1987					= 4259;
	public static final int	GCS_Manoca							= 4260;
	public static final int	GCS_Merchich						= 4261;
	public static final int	GCS_Massawa							= 4262;
	public static final int	GCS_Minna							= 4263;
	public static final int	GCS_Mhast							= 4264;
	public static final int	GCS_Monte_Mario						= 4265;
	public static final int	GCS_M_poraloko						= 4266;
	public static final int	GCS_NAD27							= 4267;
	public static final int	GCS_NAD_Michigan					= 4268;
	public static final int	GCS_NAD83							= 4269;
	public static final int	GCS_Nahrwan_1967					= 4270;
	public static final int	GCS_Naparima_1972					= 4271;
	public static final int	GCS_GD49							= 4272;
	public static final int	GCS_NGO_1948						= 4273;
	public static final int	GCS_Datum_73						= 4274;
	public static final int	GCS_NTF								= 4275;
	public static final int	GCS_NSWC_9Z_2						= 4276;
	public static final int	GCS_OSGB_1936						= 4277;
	public static final int	GCS_OSGB70							= 4278;
	public static final int	GCS_OS_SN80							= 4279;
	public static final int	GCS_Padang							= 4280;
	public static final int	GCS_Palestine_1923					= 4281;
	public static final int	GCS_Pointe_Noire					= 4282;
	public static final int	GCS_GDA94							= 4283;
	public static final int	GCS_Pulkovo_1942					= 4284;
	public static final int	GCS_Qatar							= 4285;
	public static final int	GCS_Qatar_1948						= 4286;
	public static final int	GCS_Qornoq							= 4287;
	public static final int	GCS_Loma_Quintana					= 4288;
	public static final int	GCS_Amersfoort						= 4289;
	public static final int	GCS_RT38							= 4290;
	public static final int	GCS_SAD69							= 4291;
	public static final int	GCS_Sapper_Hill_1943				= 4292;
	public static final int	GCS_Schwarzeck						= 4293;
	public static final int	GCS_Segora							= 4294;
	public static final int	GCS_Serindung						= 4295;
	public static final int	GCS_Sudan							= 4296;
	public static final int	GCS_Tananarive						= 4297;
	public static final int	GCS_Timbalai_1948					= 4298;
	public static final int	GCS_TM65							= 4299;
	public static final int	GCS_TM75							= 4300;
	public static final int	GCS_Tokyo							= 4301;
	public static final int	GCS_Trinidad_1903					= 4302;
	public static final int	GCS_TC_1948							= 4303;
	public static final int	GCS_Voirol_1875						= 4304;
	public static final int	GCS_Voirol_Unifie					= 4305;
	public static final int	GCS_Bern_1938						= 4306;
	public static final int	GCS_Nord_Sahara_1959				= 4307;
	public static final int	GCS_Stockholm_1938					= 4308;
	public static final int	GCS_Yacare							= 4309;
	public static final int	GCS_Yoff							= 4310;
	public static final int	GCS_Zanderij						= 4311;
	public static final int	GCS_MGI								= 4312;
	public static final int	GCS_Belge_1972						= 4313;
	public static final int	GCS_DHDN							= 4314;
	public static final int	GCS_Conakry_1905					= 4315;
	public static final int	GCS_WGS_72							= 4322;
	public static final int	GCS_WGS_72BE						= 4324;
	public static final int	GCS_WGS_84							= 4326;
	public static final int	GCS_Bern_1898_Bern					= 4801;
	public static final int	GCS_Bogota_Bogota					= 4802;
	public static final int	GCS_Lisbon_Lisbon					= 4803;
	public static final int	GCS_Makassar_Jakarta				= 4804;
	public static final int	GCS_MGI_Ferro						= 4805;
	public static final int	GCS_Monte_Mario_Rome				= 4806;
	public static final int	GCS_NTF_Paris						= 4807;
	public static final int	GCS_Padang_Jakarta					= 4808;
	public static final int	GCS_Belge_1950_Brussels				= 4809;
	public static final int	GCS_Tananarive_Paris				= 4810;
	public static final int	GCS_Voirol_1875_Paris				= 4811;
	public static final int	GCS_Voirol_Unifie_Paris				= 4812;
	public static final int	GCS_Batavia_Jakarta					= 4813;
	public static final int	GCS_ATF_Paris						= 4901;
	public static final int	GCS_NDG_Paris						= 4902;
	public static final int	GCSE_Airy1830						= 4001;
	public static final int	GCSE_AiryModified1849				= 4002;
	public static final int	GCSE_AustralianNationalSpheroid		= 4003;
	public static final int	GCSE_Bessel1841						= 4004;
	public static final int	GCSE_BesselModified					= 4005;
	public static final int	GCSE_BesselNamibia					= 4006;
	public static final int	GCSE_Clarke1858						= 4007;
	public static final int	GCSE_Clarke1866						= 4008;
	public static final int	GCSE_Clarke1866Michigan				= 4009;
	public static final int	GCSE_Clarke1880_Benoit				= 4010;
	public static final int	GCSE_Clarke1880_IGN					= 4011;
	public static final int	GCSE_Clarke1880_RGS					= 4012;
	public static final int	GCSE_Clarke1880_Arc					= 4013;
	public static final int	GCSE_Clarke1880_SGA1922				= 4014;
	public static final int	GCSE_Everest1830_1937Adjustment		= 4015;
	public static final int	GCSE_Everest1830_1967Definition		= 4016;
	public static final int	GCSE_Everest1830_1975Definition		= 4017;
	public static final int	GCSE_Everest1830Modified			= 4018;
	public static final int	GCSE_GRS1980						= 4019;
	public static final int	GCSE_Helmert1906					= 4020;
	public static final int	GCSE_IndonesianNationalSpheroid		= 4021;
	public static final int	GCSE_International1924				= 4022;
	public static final int	GCSE_International1967				= 4023;
	public static final int	GCSE_Krassowsky1940					= 4024;
	public static final int	GCSE_NWL9D							= 4025;
	public static final int	GCSE_NWL10D							= 4026;
	public static final int	GCSE_Plessis1817					= 4027;
	public static final int	GCSE_Struve1860						= 4028;
	public static final int	GCSE_WarOffice						= 4029;
	public static final int	GCSE_WGS84							= 4030;
	public static final int	GCSE_GEM10C							= 4031;
	public static final int	GCSE_OSU86F							= 4032;
	public static final int	GCSE_OSU91A							= 4033;
	public static final int	GCSE_Clarke1880						= 4034;
	public static final int	GCSE_Sphere							= 4035;

	/*
	 * prime meridian
	 */
	static public final int	PM_Greenwich						= 8901;
	static public final int	PM_Lisbon							= 8902;
	static public final int	PM_Paris							= 8903;
	static public final int	PM_Bogota							= 8904;
	static public final int	PM_Madrid							= 8905;
	static public final int	PM_Rome								= 8906;
	static public final int	PM_Bern								= 8907;
	static public final int	PM_Jakarta							= 8908;
	static public final int	PM_Ferro							= 8909;
	static public final int	PM_Brussels							= 8910;
	static public final int	PM_Stockholm						= 8911;

	private double[]		ModelPixelScale						= null;
	private double[]		ModelTiepoint						= null;
	private int[]			GeoKeyDirectory						= null;
	private double[]		GeoDoubleParams						= null;
	private String			GeoAsciiParams						= null;

	private static Log		logger								= LogFactory.getLog(GeoEntry.class);

	/**
	 * Set the scale of each pixel when converting to model space. This tag,
	 * IFDEntry.TAG_ModelPixelScale, is read from the TIFF file.
	 * 
	 * @param modelPixelScale
	 *            scale in x, y and z.
	 */
	public void setModelPixelScale(double[] modelPixelScale) {
		ModelPixelScale = modelPixelScale;
		for (int i = 1; i < ModelPixelScale.length; i += 3) {
			ModelPixelScale[i] = -ModelPixelScale[i];
		}
	}

	/**
	 * Set the tie point of the raster to the modelspace. This tag,
	 * IFDEntry.TAG_ModelTiepoint, is read from the TIFF file.
	 * 
	 * @param modelTiepoint
	 *            point that ties the raster space to the modelspace.
	 */
	public void setModelTiepoint(double[] modelTiepoint) {
		ModelTiepoint = modelTiepoint;
	}

	/**
	 * The geo directory read from the tiff file. This is similair to the IFD
	 * but contains information about the geo tags.
	 * 
	 * @param geoKeyDirectory
	 *            the directory of all geo tags.
	 */
	public void setGeoKeyDirectory(int[] geoKeyDirectory) {
		GeoKeyDirectory = geoKeyDirectory;
	}

	/**
	 * The double values used in the geokeydirectory are stored in this array.
	 * The directory will have an offset and count into this array to read the
	 * right number of entries.
	 * 
	 * @param geoDoubleParams
	 *            all the double values needed for the geotags.
	 */
	public void setGeoDoubleParams(double[] geoDoubleParams) {
		GeoDoubleParams = geoDoubleParams;
	}

	/**
	 * The string values used in the geokeydirectory are stored in this string.
	 * The directory will have an offset and count into this array to read the
	 * right number of entries. Each entry will be seperated by |.
	 * 
	 * @param geoAsciiParams
	 *            string containing all the substrings used in the directory.
	 */
	public void setGeoAsciiParams(String geoAsciiParams) {
		// logger.debug(geoAsciiParams);
		GeoAsciiParams = geoAsciiParams;
	}

	/**
	 * Checks to see if this is a valid GeoTiff image.
	 * 
	 * @return true if any geotiff attributes are set.
	 */
	public boolean validGeoTiff() {
		return ((ModelPixelScale != null) && (ModelTiepoint != null) && (GeoKeyDirectory != null));
	}

	/**
	 * Convert the parameters found in the geotags into a geoinformation. If the
	 * geotags are not valid or no geotags are present this will return null.
	 * 
	 * @return geotiff params converted to geoinformation
	 */
	// public GeoInformation getGeoInformation() {
	public Projection getGeoInformation() {
		int i, geoKey, tag, count, value, s;
		double[] d;

		if (!validGeoTiff()) {
			return null;
		}

		// check first byte, has to be 1 for now.
		if (GeoKeyDirectory[0] != 1) {
			logger.warn("Not valid GeoTiff format, geokeydirectory is invalid.");
			return null;
		}

		logger.debug("Revision = " + GeoKeyDirectory[1] + "." + GeoKeyDirectory[2]);
		logger.debug("GeoNumKeys = " + GeoKeyDirectory[3]);

		GeoParam geoParameters = new GeoParam();

		// set the tie point, only single tiepoint is supported
		if (ModelTiepoint.length < 6) {
			logger.warn("Not enough samples for tiepoint.");
			return null;
		}

		int projectionType = 0;
		boolean isGeographic = false;

		// loop through the directory parsing the keys and setting the variables
		for (i = 4; i < GeoKeyDirectory.length; i += 4) {
			geoKey = GeoKeyDirectory[i];
			tag = GeoKeyDirectory[i + 1];
			count = GeoKeyDirectory[i + 2];
			value = GeoKeyDirectory[i + 3];

			logger.debug(String.format("key=%d, tag=%d, count=%d, value=%d", geoKey, tag, count, value));

			switch (geoKey) {
			// GeoTIFF Configuration GeoKeys
			case GTModelTypeGeoKey:
				geoParameters.modeltype = getShort(tag, count, value);
				break;

			case GTRasterTypeGeoKey:
				geoParameters.rastertype = getShort(tag, count, value);
				break;

			case GTCitationGeoKey:
				geoParameters.nameprj = getString(tag, count, value);
				break;

			// Geographic CS Parameter GeoKeys
			case GeographicTypeGeoKey:
				geoParameters.geogcs = getShort(tag, count, value);
				break;

			case GeogCitationGeoKey:
				geoParameters.namegeogcs = getString(tag, count, value);
				break;

			case GeogGeodeticDatumGeoKey:
				geoParameters.datum = getShort(tag, count, value);
				break;

			case GeogPrimeMeridianGeoKey:
				geoParameters.primemeridian = getShort(tag, count, value);
				break;

			case GeogLinearUnitsGeoKey:
				geoParameters.lunit = getShort(tag, count, value);
				break;

			case GeogAngularUnitsGeoKey:
				geoParameters.aunit = getShort(tag, count, value);
				break;

			case GeogEllipsoidGeoKey:
				geoParameters.ellipsoid = getShort(tag, count, value);
				break;

			case GeogSemiMajorAxisGeoKey:
				d = getDouble(tag, count, value);
				geoParameters.major = d[0];
				break;

			case GeogSemiMinorAxisGeoKey:
				d = getDouble(tag, count, value);
				geoParameters.minor = d[0];
				break;

			case GeogInvFlatteningGeoKey:
				d = getDouble(tag, count, value);
				geoParameters.invflat = d[0];
				break;

			// Projected CS Parameter GeoKeys
			case ProjectedCSTypeGeoKey:
				geoParameters.pcs = getShort(tag, count, value);
				break;

			case PCSCitationGeoKey:
				geoParameters.namepcs = getString(tag, count, value);
				break;

			case ProjectionGeoKey:
				geoParameters.projection = getShort(tag, count, value);
				break;

			case ProjCoordTransGeoKey:
				geoParameters.projct = getShort(tag, count, value);
				break;

			case ProjLinearUnitsGeoKey:
				geoParameters.lunit = getShort(tag, count, value);
				break;

			// parameters
			case ProjCenterLongGeoKey:
				d = getDouble(tag, count, value);
				geoParameters.parameters.put(Projection.LONGITUDE_OF_CENTER, "" + d[0]);
				break;

			case ProjCenterLatGeoKey:
				d = getDouble(tag, count, value);
				geoParameters.parameters.put(Projection.LATITUDE_OF_CENTER, "" + d[0]);
				break;

			case ProjNatOriginLongGeoKey:
				d = getDouble(tag, count, value);
				geoParameters.parameters.put(Projection.LONGITUDE_OF_ORIGIN, "" + d[0]);
				break;

			case ProjNatOriginLatGeoKey:
				d = getDouble(tag, count, value);
				geoParameters.parameters.put(Projection.LATITUDE_OF_ORIGIN, "" + d[0]);
				break;

			case ProjFalseEastingGeoKey:
				d = getDouble(tag, count, value);
				geoParameters.parameters.put(Projection.FALSE_EASTING, "" + d[0]);
				break;

			case ProjFalseNorthingGeoKey:
				d = getDouble(tag, count, value);
				geoParameters.parameters.put(Projection.FALSE_NORTHING, "" + d[0]);
				break;

			case ProjScaleAtNatOriginGeoKey:
				d = getDouble(tag, count, value);
				geoParameters.parameters.put(Projection.SCALE_FACTOR, "" + d[0]);
				break;

			case ProjStdParallel1GeoKey:
				d = getDouble(tag, count, value);
				geoParameters.parameters.put(Projection.STANDARD_PARALLEL_1, "" + d[0]);
				break;

			case ProjStdParallel2GeoKey:
				d = getDouble(tag, count, value);
				geoParameters.parameters.put(Projection.STANDARD_PARALLEL_2, "" + d[0]);
				break;

			default:
				logger.info("tag is not supported : " + geoKey + " " + tag + " " + count + " " + value);
				break;
			}
		}

		// debug
		logger.debug(String.format("geoParameters.modeltype = %s", geoParameters.modeltype));
		logger.debug(String.format("geoParameters.rastertype = %s", geoParameters.modeltype));
		logger.debug(String.format("geoParameters.namepcs = %s", geoParameters.namepcs));
		logger.debug(String.format("geoParameters.pcs = %s", geoParameters.pcs));
		logger.debug(String.format("geoParameters.nameprj = %s", geoParameters.nameprj));
		logger.debug(String.format("geoParameters.projection = %s", geoParameters.projection));
		logger.debug(String.format("geoParameters.projct = %s", geoParameters.projct));
		for (Entry<String, String> entry : geoParameters.parameters.entrySet()) {
			logger.debug(String.format("geoParameters.parameters.%s = %s", entry.getKey(), entry.getValue()));
		}
		logger.debug(String.format("geoParameters.lunit = %s", geoParameters.lunit));
		logger.debug(String.format("geoParameters.geogcs = %s", geoParameters.geogcs));
		logger.debug(String.format("geoParameters.namegeogcs = %s", geoParameters.namegeogcs));
		logger.debug(String.format("geoParameters.primemeridian = %s", geoParameters.primemeridian));
		logger.debug(String.format("geoParameters.aunit = %s", geoParameters.aunit));
		logger.debug(String.format("geoParameters.datum = %s", geoParameters.datum));
		logger.debug(String.format("geoParameters.ellipsoid = %s", geoParameters.ellipsoid));
		logger.debug(String.format("geoParameters.major = %s", geoParameters.major));
		logger.debug(String.format("geoParameters.minor = %s", geoParameters.minor));
		logger.debug(String.format("geoParameters.invflat = %s", geoParameters.invflat));

		int tp_count = 0;
		for (int k = 0, j = 0; k < ModelTiepoint.length / 6; tp_count++) {
			logger.debug(String.format("tiepoint.%d.raster.x = %f", tp_count, ModelTiepoint[k++]));
			logger.debug(String.format("tiepoint.%d.raster.y = %f", tp_count, ModelTiepoint[k++]));
			logger.debug(String.format("tiepoint.%d.raster.z = %f", tp_count, ModelTiepoint[k++]));
			logger.debug(String.format("tiepoint.%d.model.x = %f", tp_count, ModelTiepoint[k++]));
			logger.debug(String.format("tiepoint.%d.model.y = %f", tp_count, ModelTiepoint[k++]));
			logger.debug(String.format("tiepoint.%d.model.z = %f", tp_count, ModelTiepoint[k++]));
			logger.debug(String.format("tiepoint.%d.scale.x = %f", tp_count, ModelPixelScale[j++]));
			logger.debug(String.format("tiepoint.%d.scale.y = %f", tp_count, ModelPixelScale[j++]));
			logger.debug(String.format("tiepoint.%d.scale.z = %f", tp_count, ModelPixelScale[j++]));
		}

		// check for geographic projection
		switch (geoParameters.modeltype) {
		case ModelTypeGeographic:
			// create a single tiepoint
			RasterPoint rp = new RasterPoint(ModelTiepoint[1], ModelTiepoint[0], ModelTiepoint[2]);
			ModelPoint mp = new ModelPoint(ModelTiepoint[3], ModelTiepoint[4], ModelTiepoint[5], getAngularUnit(geoParameters.aunit), LinearUnit.Meter);
			TiePoint tp = new TiePoint(mp, rp, ModelPixelScale[0], ModelPixelScale[1], ModelPixelScale[2]);

			// get the geogcs
			GeoGraphicCoordinateSystem geogcs = getGeoGCS(geoParameters);

			// create the projection
			try {
				Projection prj = Projection.getProjection(geoParameters.namegeogcs, ProjectionType.Geographic, geogcs);
				prj.setTiePoint(tp);
				logger.debug("Projection = \n" + prj);
				return prj;
			} catch (GeoException e) {
				logger.warn("Could not create geographic projection.", e);
				return null;
			}
		case ModelTypeGeocentric:
			logger.warn("Do not know how to create geocentric projection.");
			return null;
		case ModelTypeProjected:
			logger.debug("Model Projection.");
			break;
		}

		// create a single tiepoint
		RasterPoint rp = new RasterPoint(ModelTiepoint[1], ModelTiepoint[0], ModelTiepoint[2]);
		ModelPoint mp = new ModelPoint(ModelTiepoint[3], ModelTiepoint[4], ModelTiepoint[5]);
		TiePoint tp = new TiePoint(mp, rp, ModelPixelScale[0], ModelPixelScale[1], ModelPixelScale[2]);

		if (geoParameters.pcs == UserDefined) {
			Projection prj = getCustomProjection(geoParameters, tp);
			logger.debug("Projection = \n" + prj);
			return prj;
		} else {
			Projection prj = getStandardProjection(geoParameters, tp);
			logger.debug("Projection = \n" + prj);
			return prj;
		}
	}

	private Projection getCustomProjection(GeoParam gp, TiePoint tp) {
		if (gp.projection != UserDefined) {
			logger.warn("Projection is not supported, " + gp.projection);
			return null;
		}

		// user defined projection
		ProjectionType type = null;
		switch (gp.projct) {
		case CT_TransverseMercator:
			type = ProjectionType.Transverse_Mercator;
			break;
		case CT_LambertAzimEqualArea:
			type = ProjectionType.Lambert_Azimuthal_Equal_Area;
			break;
		case CT_LambertConfConic:
			type = ProjectionType.Lambert_Conformal_Conic;
			break;
		case CT_Sinusoidal:
			type = ProjectionType.Sinusoidal;
			break;
		case CT_AlbersEqualArea:
			type = ProjectionType.Albers;
			break;
		default:
			logger.warn("Coordinate transform is not supported, " + gp.projct);
			return null;
		}

		// geographic coordinate system
		GeoGraphicCoordinateSystem geogcs = getGeoGCS(gp);
		if (geogcs == null) {
			return null;
		}

		// create the projection
		try {
			return Projection.getProjection(gp.nameprj, type, geogcs, gp.parameters, tp, getLinearUnit(gp.lunit));
		} catch (GeoException e) {
			logger.warn("Could not create projection.", e);
			return null;
		}
	}

	private Projection getStandardProjection(GeoParam gp, TiePoint tp) {
		int zone = gp.pcs % 100;
		logger.debug(String.format("geoParameters.pcs.zone = %d", zone));
		// WGS72 / UTM northern hemisphere: 322zz where zz is UTM zone number
		if ((gp.pcs >= 32200) && (gp.pcs <= 32299)) {
			Datum wgs72 = new Datum("WGS_1972", Ellipsoid.WGS_1972);
			GeoGraphicCoordinateSystem geogcs = new GeoGraphicCoordinateSystem(wgs72);
			Map<String, String> param = new HashMap<String, String>();
			param.put(Projection.LONGITUDE_OF_CENTER, "" + TransverseMercator.computeLongitude(zone));
			param.put(Projection.FALSE_EASTING, "500000.0");
			param.put(Projection.FALSE_NORTHING, "0.0");
			param.put(Projection.SCALE_FACTOR, "0.9996");
			try {
				return Projection.getProjection(gp.nameprj, ProjectionType.Transverse_Mercator, geogcs, param, tp, getLinearUnit(gp.lunit));
			} catch (GeoException e) {
				logger.warn("Could not create projection for " + gp.pcs, e);
				return null;
			}
		}
		// WGS72 / UTM southern hemisphere: 323zz where zz is UTM zone number
		if ((gp.pcs >= 32300) && (gp.pcs <= 32399)) {

		}
		// WGS72BE / UTM northern hemisphere: 324zz where zz is UTM zone number
		if ((gp.pcs >= 32400) && (gp.pcs <= 32499)) {

		}
		// WGS72BE / UTM southern hemisphere: 325zz where zz is UTM zone number
		if ((gp.pcs >= 32500) && (gp.pcs <= 32599)) {

		}
		// WGS84 / UTM northern hemisphere: 326zz where zz is UTM zone number
		if ((gp.pcs >= 32600) && (gp.pcs <= 32699)) {
			GeoGraphicCoordinateSystem geogcs = new GeoGraphicCoordinateSystem(Datum.WGS_1984);
			Map<String, String> param = new HashMap<String, String>();
			param.put(Projection.LONGITUDE_OF_CENTER, "" + TransverseMercator.computeLongitude(zone));
			param.put(Projection.FALSE_EASTING, "500000.0");
			param.put(Projection.FALSE_NORTHING, "0.0");
			param.put(Projection.SCALE_FACTOR, "0.9996");
			try {
				return Projection.getProjection(gp.nameprj, ProjectionType.Transverse_Mercator, geogcs, param, tp, getLinearUnit(gp.lunit));
			} catch (GeoException e) {
				logger.warn("Could not create projection for " + gp.pcs, e);
				return null;
			}
		}
		// WGS84 / UTM southern hemisphere: 327zz where zz is UTM zone number
		if ((gp.pcs >= 32700) && (gp.pcs <= 32799)) {

		}
		// US State Plane (NAD27): 267xx/320xx
		if (((gp.pcs >= 26700) && (gp.pcs <= 26799)) || ((gp.pcs >= 32000) && (gp.pcs <= 32099))) {

		}
		// US State Plane (NAD83): 269xx/321xx
		if (((gp.pcs >= 26900) && (gp.pcs <= 26999)) || ((gp.pcs >= 32100) && (gp.pcs <= 32199))) {
			GeoGraphicCoordinateSystem geogcs = new GeoGraphicCoordinateSystem(Datum.North_American_1983_Conus);
			Map<String, String> param = new HashMap<String, String>();
			param.put(Projection.FALSE_EASTING, "500000.0");
			param.put(Projection.FALSE_NORTHING, "0.0");
			param.put(Projection.SCALE_FACTOR, "0.9996");
			switch (gp.pcs) {
			case PCS_NAD83_UTM_zone_3N:
			case PCS_NAD83_UTM_zone_4N:
			case PCS_NAD83_UTM_zone_5N:
			case PCS_NAD83_UTM_zone_6N:
			case PCS_NAD83_UTM_zone_7N:
			case PCS_NAD83_UTM_zone_8N:
			case PCS_NAD83_UTM_zone_9N:
			case PCS_NAD83_UTM_zone_10N:
			case PCS_NAD83_UTM_zone_11N:
			case PCS_NAD83_UTM_zone_12N:
			case PCS_NAD83_UTM_zone_13N:
			case PCS_NAD83_UTM_zone_14N:
			case PCS_NAD83_UTM_zone_15N:
			case PCS_NAD83_UTM_zone_16N:
			case PCS_NAD83_UTM_zone_17N:
			case PCS_NAD83_UTM_zone_18N:
			case PCS_NAD83_UTM_zone_19N:
			case PCS_NAD83_UTM_zone_20N:
			case PCS_NAD83_UTM_zone_21N:
			case PCS_NAD83_UTM_zone_22N:
			case PCS_NAD83_UTM_zone_23N:
				param.put(Projection.LONGITUDE_OF_CENTER, "" + TransverseMercator.computeLongitude(zone));
				break;
			default:
				logger.warn("Unknown US State Plane (NAD83): 269xx/321xx, " + gp.pcs);
				return null;
			}
			try {
				return Projection.getProjection(gp.nameprj, ProjectionType.Transverse_Mercator, geogcs, param, tp, getLinearUnit(gp.lunit));
			} catch (GeoException e) {
				logger.warn("Could not create projection for " + gp.pcs, e);
				return null;
			}
		}

		logger.warn("Could not create projection for " + gp.pcs);
		return null;
	}

	private GeoGraphicCoordinateSystem getGeoGCS(GeoParam gp) {
		switch (gp.geogcs) {
		case 0:
			logger.debug("GeoGCS is undefined.");
			return null;
		case UserDefined:
			Datum datum = getDatum(gp);
			if (datum == null) {
				return null;
			}
			PrimeMeridian pm = getPrimeMeridian(gp);
			if (pm == null) {
				return null;
			}
			return new GeoGraphicCoordinateSystem(gp.namegeogcs, datum, pm, getAngularUnit(gp.aunit));
		case GCS_WGS_84:
			return new GeoGraphicCoordinateSystem(gp.namegeogcs, Datum.WGS_1984);
		case GCS_NAD83:
			return new GeoGraphicCoordinateSystem(gp.namegeogcs, Datum.North_American_1983_Conus);
		default:
			logger.warn(String.format("Can not create geogcs for %d.", gp.geogcs));
			return null;
		}
	}

	private Datum getDatum(GeoParam gp) {
		switch (gp.datum) {
		case Datum_WGS84:
			return Datum.WGS_1984;
		default:
			logger.warn("Could not create datum " + gp.datum);
			return null;
		}
	}

	private PrimeMeridian getPrimeMeridian(GeoParam gp) {
		switch (gp.primemeridian) {
		case PM_Greenwich:
			return PrimeMeridian.Greenwich;
		default:
			logger.warn("Could not create prime meridian " + gp.primemeridian);
			return null;
		}
	}

	private AngularUnit getAngularUnit(int val) {
		switch (val) {
		case Angular_Radian:
			return AngularUnit.Radian;
		case Angular_Degree:
			return AngularUnit.Decimal_Degree;
		case Angular_Arc_Minute:
			return AngularUnit.Decimal_Minute;
		case Angular_Arc_Second:
			return AngularUnit.Decimal_Second;
		case Angular_Grad:
			return AngularUnit.Grad;
		case Angular_Gon:
			return AngularUnit.Gon;
			// case Angular_DMS:
			// return AngularUnit.Radian;
			// case Angular_DMS_Hemisphere:
			// return AngularUnit.Radian;
		default:
			logger.warn("No support for angular type " + val);
			return AngularUnit.Radian;
		}
	}

	private LinearUnit getLinearUnit(int val) {
		switch (val) {
		case Linear_Meter:
			return LinearUnit.Meter;
		case Linear_Foot:
			return LinearUnit.Foot;
		case Linear_Foot_US_Survey:
			return LinearUnit.US_Foot;
		case Linear_Foot_Modified_American:
			return LinearUnit.Modified_American_Foot;
		case Linear_Foot_Clarke:
			return LinearUnit.Clarkes_Foot;
		case Linear_Foot_Indian:
			return LinearUnit.Indian_Foot;
		case Linear_Link:
			return LinearUnit.Link;
		case Linear_Link_Benoit:
			return LinearUnit.Link_Benoit;
		case Linear_Link_Sears:
			return LinearUnit.Link_Sears;
		case Linear_Chain_Benoit:
			return LinearUnit.Chain_Benoit;
		case Linear_Chain_Sears:
			return LinearUnit.Chain_Sears;
		case Linear_Yard_Sears:
			return LinearUnit.Yard_Sears;
		case Linear_Yard_Indian:
			return LinearUnit.Yard_Indian;
		case Linear_Fathom:
			return LinearUnit.Fathom;
		case Linear_Mile_International_Nautical:
			return LinearUnit.Nautical_Mile;
		default:
			logger.warn("No support for linear type " + val);
			return LinearUnit.Meter;
		}
	}

	/**
	 * Read a single short value out of the directory. This checks to make sure
	 * only a single entry is requested, and tag is set to 0.
	 * 
	 * @param tag
	 *            the TiffTag that is used to get information from, or 0 if the
	 *            information is stored in the geodirectory.
	 * @param count
	 *            the number of entries to return.
	 * @param value
	 *            the value to return.
	 * @return
	 */
	private int getShort(int tag, int count, int value) {
		if ((tag != 0) || (count != 1)) {
			// logger.warn("Expected a single value of short");
		}
		return value;
	}

	/**
	 * Read entries from the doubleparams array. This checks to make sure that
	 * the entries come from the double params.
	 * 
	 * @param tag
	 *            the TiffTag that is used to get information from, or 0 if the
	 *            information is stored in the geodirectory.
	 * @param count
	 *            the number of entries to return.
	 * @param offset
	 *            the offset into the doubleparams to start at.
	 * @return an array of double entries copied from the doubleparams
	 */
	private double[] getDouble(int tag, int count, int offset) {
		if (tag != IFDEntry.TAG_GeoDoubleParams) {
			// logger.warn("Expected a string value.");
			return new double[count];
		}
		double[] result = new double[count];
		System.arraycopy(GeoDoubleParams, offset, result, 0, count);
		return result;
	}

	/**
	 * Read entries from the asciiparams string. This checks to make sure that
	 * the entries come from the ascii params and the last char is |.
	 * 
	 * @param tag
	 *            the TiffTag that is used to get information from, or 0 if the
	 *            information is stored in the geodirectory.
	 * @param count
	 *            the number of characters to return.
	 * @param offset
	 *            the offset into the asciiparams to start at.
	 * @return a substring of the asciiparams with the last | replaced by \0.
	 */
	private String getString(int tag, int count, int offset) {
		int end = offset + count - 1;
		if (tag != IFDEntry.TAG_GeoAsciiParams) {
			// logger.warn("Expected a string value.");
			return "";
		}
		if (end >= GeoAsciiParams.length()) {
			// logger.warn("Not enough chars in string.");
			end = GeoAsciiParams.length() - 1;
		}
		if (GeoAsciiParams.charAt(end) != '|') {
			// logger.debug("Expected a | as last value.");
		}
		return GeoAsciiParams.substring(offset, end);
	}

	class GeoParam {
		public int			modeltype		= 0;
		public int			rastertype		= 0;

		// projection coordinate system
		String				namepcs			= "";
		int					pcs				= 0;

		// projection
		String				nameprj			= "";
		int					projection		= 0;
		int					projct			= 0;
		Map<String, String>	parameters		= new HashMap<String, String>();
		int					lunit			= 0;

		// geographic coordinate system
		String				namegeogcs		= "";
		int					geogcs			= 0;
		int					primemeridian	= 0;
		int					aunit			= 0;

		// datum
		int					datum			= 0;

		// ellipsoid
		int					ellipsoid		= 0;
		double				major			= 0;
		double				minor			= 0;
		double				invflat			= 0;
	}
}
