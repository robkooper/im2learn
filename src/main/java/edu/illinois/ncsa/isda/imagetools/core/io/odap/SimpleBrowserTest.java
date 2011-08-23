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
package edu.illinois.ncsa.isda.imagetools.core.io.odap;

/**
 * Tests the simple web browser's capabilities.
 * 
 * @author Yakov Keselman
 * @version June 15, 2006.
 */
public class SimpleBrowserTest {
	
	/**
	 * Testing the browser. 
	 * @param args
	 */
	public static void main(String[] args) {    
	
		// NOTE: all URL's must start with an "http://"
		
		String url = "http://daac.gsfc.nasa.gov/services/dods/modis_terra_dp.shtml";
		// String url = "http://g0dup05u.ecs.nasa.gov/opendap-bin/nph-dods/OPENDAP_DP/short_term/MOGT/MOD03.005/2001.11.26/MOD03.A2001330.0435.005.2006155142713.hdf";
		// String url = "http://g0dup05u.ecs.nasa.gov/opendap-bin/nph-dods/OPENDAP_DP/short_term/MOGT/MOD02QKM.005/2005.10.25/MOD02QKM.A2005298.1610.005.2005299095356.hdf";
		// String url = "http://g0dup05u.ecs.nasa.gov/opendap-bin/nph-dods/OPENDAP_DP/short_term/MOGT/MOD02QKM.005/2005.10.25/MOD02QKM.A2005298.1610.005.2005299095356.hdf.dods";
		// String url = "http://g0dup05u.ecs.nasa.gov/opendap-bin/nph-dods/OPENDAP_DP/short_term/MOGT/MOD02QKM.005/2005.10.25/MOD02QKM.A2005298.1610.005.2005299095356.hdf.html";
		// String url = "http://g0dup05u.ecs.nasa.gov/OPENDAP_DP/AIRS/AIRH2CCF.003/2002.09.02/";
		// String url = "http://www.yahoo.com";
		// String url = "http://g0dup05u.ecs.nasa.gov/opendap-bin/nph-dods/OPENDAP_DP/short_term/MOGT/MOD03.005/";
	    SimpleBrowser browser = new SimpleBrowser(null, url );
	    browser.equals( null );
	  }
}
