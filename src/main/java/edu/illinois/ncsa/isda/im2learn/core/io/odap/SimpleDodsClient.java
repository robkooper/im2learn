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

import dods.dap.*;
import dods.dap.parser.*;

import java.io.*;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A class that implements a minimal DODS client functionality.
 * Based mostly on "Geturl.java" code that came with DODS/ODAP distribution.
 * @author Yakov Keselman
 * @version June 17, 2006.
 */
public class SimpleDodsClient {
		
	/**
	 * The logger.
	 */
	static protected Log logger = LogFactory.getLog(TypedFile.class);

	/**
	 * Fetching the data object from the URL via DODS and saving it in a file.
	 * @param urlString the URL to fetch data from.
	 * @param fileName of the file to save data in.
	 * @param compress
	 */
	public static void loadDataDODS( String urlString, String fileName, boolean compress) 
	throws IOException {
		
		// get a connection to the data object.
		DConnect url = new DConnect( urlString, compress );

		try
		{
			// get the data.
			DataDDS dds = url.getData( null );
			
			// save the data.
			FileOutputStream out = new FileOutputStream(fileName);
			dds.externalize(out, compress, true);
		}
		catch( TokenMgrError e )	// this must not have been derived from an Exception!
		{
			throw new IOException( e.toString() );
		}
		catch( Exception e )
		{
			throw new IOException( e.toString() );
		}
	}

	
	/**
	 * Fetching the data object from the URL via HTTP and saving it in a file.
	 * Throws an exception if something does not go well.
	 * 
	 * @param urlString
	 *            the URL to fetch data from.
	 * @param fileName
	 *            of the file to save data in.
	 */
	public static void loadDataHTTP( String urlString, String fileName ) 
		throws IOException
	{
		try
		{
			// open in and out streams.
			byte[] buffer = new byte[10240];
			URL dodsDataURL = new URL( urlString);
			logger.info( "Trying: " + dodsDataURL );
			InputStream urlStream = dodsDataURL.openStream();
			FileOutputStream out = new FileOutputStream(fileName);
			
    		// read and write data.		
			int len = 0;
			while((len = urlStream.read(buffer)) != -1 )
				out.write(buffer, 0, len);
			out.flush();
			urlStream.close();
			out.close();
		}
		catch( Exception e )
		{
			throw new IOException( e.toString() );
		}
	}

}
