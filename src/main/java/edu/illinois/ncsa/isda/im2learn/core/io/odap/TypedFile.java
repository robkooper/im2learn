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
package edu.illinois.ncsa.isda.im2learn.core.io.odap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Parsing and interpreting the kind of file that was given to us.
 * 
 * @author Yakov Keselman
 * @version June 16, 2006.
 */
public class TypedFile {
	
	/**
	 * The logger.
	 */
    static protected Log logger = LogFactory.getLog( TypedFile.class );

    
	/**
	 * The various types that the File can be.
	 */
	public static enum Filetype 
	{ 
		HTML_NAVIGATION,// plain html files or directories that can be used for navigation.
		TEXT_DIRECTORY,	// text files that contain HTML links (can be used to get HTML links).
		DODS_DATA,		// data of useful type (e.g., .hdf.dods) that should be sent to GeoLearn.
		DODS_INFO,		// dods info of potentially useful type (e.g., .hdf.das).
		DATA,			// data of useful type (e.g., .hdf) that should be sent to GeoLearn.
		UNKNOWN,		// files of unknown types. We'll try to process them somehow.
		IGNORED			// files that should be ignored (such as .jpeg).
	};
		
	/**
	 * Suffixes of data files. Add your own to extend the functionality.
	 */
	protected static String dataSuffix[] = { 
		"hdf", "tiff", "tif", "eos", "dem", "jpeg", "jpg", "gif" };

	/**
	 * Info suffixes understood by OpenDAP/DODS servers.
	 * Can be used to get DODS info via a browser or other non-ODAP clients.
	 */
	protected static String dodsInfoSuffix[] = { 
		"version", "das", "dds", "ascii", "info" };
	
	/**
	 * Data suffixes understood by OpenDAP/DODS servers.
	 * Can be used to get DODS data via a browser or other non-ODAP clients.
	 */
	protected static String dodsDataSuffix = "dods";
	
	/**
	 * Directory suffixes.
	 */
	protected static String directorySuffix[] = { 
		"asc", "dat.asc", "txt", "text", "xml" };
	
	
	/**
	 * Suffixes of files that should be ignored. Add your own to extend the functionality.
	 */
	protected static String ignoredSuffix[] = {};

	
	/**
	 * String representation of the File.
	 */
	protected String fileName;
	
	/**
	 * The type of the file, one of the enumerated above.
	 */
	protected Filetype fileType;

	/**
	 * The last suffix.
	 */
	protected String suffix0;
	
	/**
	 * The suffix just before the last one.
	 */
	protected String suffix1;
	
	/**
	 * The type of the data.
	 */
	protected String dataType = "";
	
	
	/**
	 * @param url to process.
	 */
	public TypedFile( String url )
	{
		// extract the last element of the URL.
		String[] component = url.split( "[\\/]" );
		if( component.length > 0 )
			fileName = component[component.length-1];
		else
			fileName = "";

		// split the name into components, and set file type based on that.
		determineSuffixes();
		setFileType();
	}

	
	/**
	 * @return the type of the File (one of the defined above).
	 * Note: when it returns Filetype.DATA, it might mean data on a regular server
	 * or data on a DODS server. A DODS server will return an error. The easiest way
	 * to differentiate between the two is to append ".dods" to the file name. A regular
	 * server will return an error; a DODS server will return data.
	 */
	public Filetype getFileType()
	{
		return fileType;
	}

	/**
	 * @return the type of the data ( "hdf", "tiff", etc.)
	 */
	public String getDataType()
	{
		return dataType;
	}
	
	/**
	 * Return the name under which the file is to be saved.
	 */
	public String getSaveAs()
	{
		if( fileType == Filetype.DATA )
			return fileName;
		else
			return replaceLastSuffix( this.fileName, "" );
	}
	
	/**
	 * Return a text suffix of DODS.
	 */
	public static String getDodsInfoSuffix()
	{
		return ".version";
	}
	
	/**
	 * Return a data suffix of DODS.
	 */
	public static String getDodsDataSuffix()
	{
		return ".dods";
	}
	
	
	/**
	 * Split the string into components, figuring out the two suffixes, suffix0, suffix1.
	 */
	protected void determineSuffixes ()
	{
		// split the string into components.
		String[] component = this.fileName.split( "[.]" );
		
		// figure out the last suffix. 
		if( component.length > 0 )
			suffix0 = component[component.length-1];
		else
			suffix0 = "";
		
		// figure out the suffix before the last one.
		if( component.length > 1 )
			suffix1 = component[component.length-2];
		else
			suffix1 = "";
	}

	
	/**
	 * @return the type of the File (among the defined ones).
	 */
	protected void setFileType()
	{
		// start out with unknown values.
		fileType = Filetype.UNKNOWN;
		dataType = "";
		
		// check if this should be ignored.
		if( isIgnored() )
		{
			fileType = Filetype.IGNORED;
			return;
		}
		
		// check if this is a directory.
		if( isDirectory() )
		{
			fileType = Filetype.TEXT_DIRECTORY;
			return;
		}
		
		// check if this is DODS html (contains JavaScript).
		// might try to handle it differently in the future.
		if( isDodsHTML () )
		{
			fileType = Filetype.DODS_DATA;
			dataType = suffix1;
			return;
		}
		
		// check if this is dods text.
		if( isDodsInfo() )
		{
			fileType = Filetype.DODS_INFO;
			dataType = suffix1;
			return;
		}
		
		// check if this is dods data.
		if( isDodsData() )
		{
			fileType = Filetype.DODS_DATA;
			dataType = suffix1;
			return;
		}

		// check if this is plain data (can be a pointer to a real file or a DODS stub).
		if( isData() )
		{
			fileType = Filetype.DATA;
			dataType = suffix0;
			return;
		}
		
		// check if this is plain html
		if( isHTML() )
		{
			fileType = Filetype.HTML_NAVIGATION;
			return;
		}
		
	}


	/**
	 * @return lower-cased suffix contains "htm" (because there are many types of
	 * html files).
	 * @param suffix of the file.
	 */
	protected boolean isHTML()
	{
		return (suffix0.toLowerCase()).indexOf( "htm" ) >= 0;
	}


	/**
	 * @return lower-cased suffix contains "htm".
	 * @param suffix of the file.
	 */
	protected boolean isDodsHTML()
	{
		return isHTML() & isAmong( suffix1, dataSuffix );
	}
	
	
	/**
	 * @return lower-cased suffix is one of the data suffixes.
	 */
	public boolean isData()
	{
		return isAmong( suffix0, dataSuffix )  | 
		isAmong( suffix1 + "." + suffix0, dataSuffix );
	}

	
	/**
	 * @return lower-cased suffix is one of the DODS text suffixes.
	 */
	public boolean isDodsInfo()
	{
		return isAmong( suffix1, dataSuffix ) & isAmong( suffix0, dodsInfoSuffix );
	}


	/**
	 * @return lower-cased suffix is one of the DODS data suffixes.
	 */
	public boolean isDodsData()
	{
		return isAmong( suffix1, dataSuffix ) & suffix0.equals( dodsDataSuffix );
	}


	/**
	 * @return lower-cased suffix is one the directory suffixes.
	 */
	public boolean isDirectory()
	{
		return 	isAmong( suffix0, directorySuffix ) | 
				isAmong( suffix1 + "." + suffix0, directorySuffix );
	}

	
	/**
	 * @return lower-case suffix is one of the ignored suffixes.
	 */
	public boolean isIgnored()
	{
		return isAmong( suffix0, ignoredSuffix );
	}
	
	
	/**
	 * Return if a lower-cased version of the string is among ones in the array.
	 */
	protected static boolean isAmong( String s, String array[] )
	{
		String sLowerCased = s.toLowerCase();
		for( int i=0; i < array.length; i++ )
			if( sLowerCased.equals( array[i].toLowerCase() ) )
				return true;
		return false;
	}


	/**
	 * Replace the last suffix with another suffix, needed for .dods.
	 * TODO: rewrite this method to be more efficient.
	 */
	public static String replaceLastSuffix( String toReplace, String newSuffix )
	{
		// split the string into components.
		String[] component = toReplace.split( "[.]" );
		String lastSuffix;
		
		// figure out the last suffix. 
		if( component.length > 0 )
			lastSuffix = component[component.length-1];
		else
			lastSuffix = "";
	
		// return a replacement.
		return toReplace.substring( 0, toReplace.length()-1-lastSuffix.length() ) + newSuffix;
	}

}
