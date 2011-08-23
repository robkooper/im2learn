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

import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.*;

import java.io.*;
import javax.swing.event.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.display.FileMenu;
import edu.illinois.ncsa.isda.imagetools.core.display.Im2LearnMainFrame;
import edu.illinois.ncsa.isda.imagetools.core.display.Im2LearnMenu;
import edu.illinois.ncsa.isda.imagetools.core.display.ImagePanel;
import edu.illinois.ncsa.isda.imagetools.core.display.ImageUpdateEvent;
import edu.illinois.ncsa.isda.imagetools.core.display.SimpleBrowser;
import edu.illinois.ncsa.isda.imagetools.core.io.LoadSaveImagePanel;
import edu.illinois.ncsa.isda.imagetools.core.io.hdf.FileChooserHDF;
import edu.illinois.ncsa.isda.imagetools.ext.conversion.AlbedoLoaderDialog;

import java.net.*;

import java.util.HashMap;

/**
 * Implements a simple web browser that can be used to navigate to files
 * on HTTP and OpenDAP/DODS servers; based heavily on javax.swing.JEditorPane.
 * 
 * We will differentiate between 5 types of files: 
 * - useful data (.hdf, .tiff), which is to be passed on to a GeoLearn component;
 *   the name might need a slight manipulation if it sits on a DODS server.
 * - useless data (.pdf, .jpeg), which is to be ignored (not loaded);
 * - navigational files (.html, directories), which will be displayed.
 * - text directories, which can be manipulated into HTML on the fly.
 * - unknown: will display as it is (no processing whatsoever).
 * 
 * TODO: think about this classification and what to do with different types of files.
 * 
 * The appropriate file extensions are defined in a helper class, TypedFile.java.
 * In the future, there will be an interface to modify extensions for the types.
 * 
 * @author Yakov Keselman
 * @version June 15, 2006.
 */


public class SimpleBrowser implements HyperlinkListener, ActionListener, Im2LearnMenu {
	
	/**
	 * The logger.
	 */
    static private Log logger = LogFactory.getLog( SimpleBrowser.class );

    	
	/**
	 * The web webPane in which everything will be displayed.
	 */
	protected JEditorPane webPane;
	
	/**
	 * Fields associated with handling URL's, especially relative.
	 */
	protected String URLprotocol = "http";
	protected String URLhost = "";
	protected String URLpath = "";
	
	protected String result = null;
	
	/**
	 * The home URL.
	 */
	protected String homeURL = "";
	
	/**
	 * The area in the browser for entering URLs.
	 */
	protected JComboBox urlComboArea;
	
	/**
	 * Keeping track of seen URL's.
	 */
	protected HashMap< String, String > seenURLs = new HashMap< String, String >();

	
	private JDialog browserFrame;


    private ImagePanel imagepanel;


	/**
	 * Initializing the browser with an url.
	 * @param url of the page to display.
	 */
	public SimpleBrowser(Frame parent,  String url )
	{
		// set up the interface first.
		setUpWebInterface(parent, url );
	}
	
    /**
     * Sets up the interface used for browsing.
     * TODO: better layout (make url area of fixed size, no matter what).
     */
    private void setUpWebInterface(Frame parent, String initialURL )
    {
		// the Home button.
		JButton homeButton = new JButton( "Home" );
		homeButton.setActionCommand( "Home" );
		homeButton.addActionListener( this );
		
		// the URL area. // TODO need to experiment more with this.
		this.urlComboArea = new JComboBox( new String[] { initialURL } );
		seenURLs.put( initialURL, initialURL );
		this.urlComboArea.setEditable( true );
		JScrollPane urlArea = new JScrollPane( this.urlComboArea,
				JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
        
		// the Go button.
		JButton goButton = new JButton( "Go" );
		goButton.setActionCommand( "Go" );
		goButton.addActionListener( this );
				
		// create the web webPane that reacts to hyperlink clicks.
		webPane = new JEditorPane();
		webPane.setEditable( false );
		webPane.addHyperlinkListener( this );
		JScrollPane webScrollPane = new JScrollPane( webPane );
		webScrollPane.setPreferredSize(new Dimension(400, 300));
	    
		// the top-level frame.
		browserFrame = new JDialog(parent, "DODS Aware Web Browser", true);

		// the navigational subframe, with its layout horizontal.
		JPanel navSubFrame = new JPanel();
		navSubFrame.setLayout( new BoxLayout( navSubFrame, BoxLayout.LINE_AXIS ) );
		navSubFrame.add( homeButton );
		navSubFrame.add( urlArea );
		navSubFrame.add( goButton );

		// get the container inside the Frame, and make its layout border (alt: vertical).
		// IMPORTANT: operations should be performed on the Frame's ContentPane.
		Container cont = browserFrame.getContentPane();
		cont.setLayout( new BorderLayout() );

		// add the components: navigation on top, web at the bottom.
		cont.add( navSubFrame, BorderLayout.PAGE_START );
		cont.add( webScrollPane, BorderLayout.CENTER ); // this is another alternative.
		
		// below is an alternative to the above line.
		// JPanel webSubFrame = new JPanel();
		// webSubFrame.setLayout( new BoxLayout( webSubFrame, BoxLayout.PAGE_AXIS ) );
		// webSubFrame.add( webScrollPane );
		// cont.add( webSubFrame, BorderLayout.CENTER );
		
		// display the URL.
		this.processURL( initialURL );
		this.homeURL = initialURL;


		// invoke the layout manager and show the window.
		browserFrame.pack();
    }
    
    public String showDialog() {
    	result = null;
    	browserFrame.setVisible( true );		
		return result;
    }

    
	/**
	 * Reacting to clicks onto links: updating the page by processing the URL.
	 */
	public void hyperlinkUpdate( HyperlinkEvent evt )
	{
		if( evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED )
		{
			String url = evt.getDescription();
			if( url != null ) processURL( evt.getDescription() );
		}
	}
	

	/**
	 * Listening to button clicks.
	 * TODO: add "forward" and "back" buttons.
	 */
    public void actionPerformed( ActionEvent e )
    {
    	// process the press of the "Home" button.
        if( "Home".equals( e.getActionCommand() ) )
        {
        	this.processURL( homeURL );
        	return;
        }
        
    	// process the press of the "Go" button.
        if( "Go".equals( e.getActionCommand() ) )
        {
        	this.processURL( (String)this.urlComboArea.getSelectedItem() );
        	return;
        }
    }


    /**
     * Updating URL's in response to a successful navigation.
     */
    private void updateURLs( String url )
    {
    	// update the url area.
    	urlComboArea.setSelectedItem( url );
    	
        // update urls inside the hash table and inside the URL area.
    	if( seenURLs.get( url ) == null )
    	{
    		seenURLs.put( url, url );
    		urlComboArea.insertItemAt( url, // last visited first.
    				urlComboArea.getItemCount()-1 );
    		// this.urlComboArea.addItem( url ); // first visited first.
    	}
    }
    
    
	/**
	 * Processing a hyperlink obtained.
	 * @param url that was clicked.
	 */
	private void processURL( String url )
	{
		browserFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		// get a URL out of the string.
	    URL u = this.getURL( url );
	    
	    // validity check.
	    if( u == null )
	    {
	    	logger.info( "Please check your URL: " + url );
	    	browserFrame.setCursor(Cursor.getDefaultCursor());
	    	return;
	    }
	    
	    // assume a valid url.
	    url = u.toString();
	    
	    // figure out the type of the file.
	    TypedFile typedFile = new TypedFile( url );
	    TypedFile.Filetype fileType = typedFile.getFileType();
	    
	    // now go into cases depending on the type of the file.
	    
	    // process files that are to be ignored.
	    if( fileType == TypedFile.Filetype.IGNORED )
	    {
	    	this.showInWebPane( "Ignoring<br>" + url + "<br>because of incompatible file type." );
	    	browserFrame.setCursor(Cursor.getDefaultCursor());
	    	return;
	    }
	    
	    
	    // process files that are to be loaded via a DODS server.
	    // strip the suffix, as it is not needed for a DODS server.
	    // a DODS server correctly responds to DODS clients, but not to HTTP clients.
	    if( 	(fileType == TypedFile.Filetype.DODS_DATA) |
	    		(fileType == TypedFile.Filetype.DODS_INFO) )
	    {
	    	url = TypedFile.replaceLastSuffix( url, "" );
	    }

	    
	    // process data files that are to be loaded, perhaps via a DODS server.
	    if( fileType == TypedFile.Filetype.DATA )
	    {
	    	// see if we can distinguish between DODS and non-DODS.
	    	
			logger.debug( "Trying url: " + url );
			
	    	try
	    	{
	    		// if succeeded here, the server is DODS.
	    		URL dodsDataURL = new URL( url + TypedFile.getDodsInfoSuffix() );
	    		dodsDataURL.openStream();
	    		
	    		try
	    		{
	    			// if succeeded here (which is never), retrieved data via a client.
		   			SimpleDodsClient.loadDataDODS( url, typedFile.getSaveAs(), true ); // false
			    	logger.info( "Saved " + url + " from a DODS server as " + typedFile.getSaveAs() + " via DODS" );
					if (typedFile.getDataType().equals("hdf")) {
						FileChooserHDF fc = new FileChooserHDF(typedFile.getSaveAs());
						if (fc.showDialog() != null) {
	    					result = fc.getFileName();
	    					browserFrame.setVisible(false);
	    			    	browserFrame.setCursor(Cursor.getDefaultCursor());
	    					return;
						}
					} else {
    					result = typedFile.getSaveAs();
    					browserFrame.setVisible(false);
    			    	browserFrame.setCursor(Cursor.getDefaultCursor());
    					return;
					}
	    		}
	    		catch( Exception badDodsClient )
	    		{
	    			// try accessing data via other (non-dods) clients.
	    			try
	    			{
    					SimpleDodsClient.loadDataHTTP( url, typedFile.getSaveAs() );
    					logger.info( "Saved " + url + " as " + typedFile.getSaveAs() );
    					if (typedFile.getDataType().equals("hdf")) {
    						FileChooserHDF fc = new FileChooserHDF(typedFile.getSaveAs());
    						if (fc.showDialog() != null) {
    	    					result = fc.getFileName();
    	    					browserFrame.setVisible(false);
    	    			    	browserFrame.setCursor(Cursor.getDefaultCursor());
    	    					return;
    						}
    					} else {
	    					result = typedFile.getSaveAs();
	    					browserFrame.setVisible(false);
	    			    	browserFrame.setCursor(Cursor.getDefaultCursor());
	    					return;
    					}
	    			}
	    			catch( Exception e )
	    			{
	    				logger.info( "Failed to load " + url, e);
	    			}
	    		}
	    	}
	    	// if we did not succeed initially, the server is not DODS, try the other possibility.
	    	catch( IOException noDodsServer )
	    	{
	    		try
	    		{
					SimpleDodsClient.loadDataHTTP( url, typedFile.getSaveAs() );
					logger.info( "Saved " + url + " as " + typedFile.getSaveAs() );	    					
					if (typedFile.getDataType().equals("hdf")) {
						FileChooserHDF fc = new FileChooserHDF(typedFile.getSaveAs());
						if (fc.showDialog() != null) {
	    					result = fc.getFileName();
	    					browserFrame.setVisible(false);
	    			    	browserFrame.setCursor(Cursor.getDefaultCursor());
	    					return;
						}
					} else {
    					result = typedFile.getSaveAs();
    					browserFrame.setVisible(false);
    			    	browserFrame.setCursor(Cursor.getDefaultCursor());
    					return;
					}
	    		}
	    		catch( IOException couldNotSave )
	    		{
	    			logger.info( "Did not save " + url + " due to IO errors.", couldNotSave);
	    		}
	    		
	    		/*
				showInWebPane( 
						"Click on the name below to load the file from a non-DODS server."
						+ linkTo( url, typedFile.getSaveAs() ) );
				*/
	    	}
	    	browserFrame.setCursor(Cursor.getDefaultCursor());
			return;
	    }

	    
	    // process files that are directories.
	    if( fileType == TypedFile.Filetype.TEXT_DIRECTORY )
	    {
	    	this.showInWebPane( "Extracted web links:" + this.getWebLinks( u ) );
	    	browserFrame.setCursor(Cursor.getDefaultCursor());
			return;
	    }
	    

	    // here, the url is none of the above types. Try showing it the way it is.
	    try
	    {
			// this allows for more complex HTML processing.
			EditorKit htmlKit = webPane.getEditorKitForContentType( "text/html" );
			HTMLDocument doc = (HTMLDocument) htmlKit.createDefaultDocument();
		    webPane.setEditorKit( htmlKit );
	        InputStream in = u.openStream();
	        
	        webPane.read( in, doc );
	        this.updateURLs( url );
	    }
	    catch( Exception e )
	    {
	    	// this is more simple but more robust HTML processing.
	    	try
	    	{
	    		webPane.setPage( url );
		        this.updateURLs( url );
	    	}
	    	catch( IOException e1 )
	    	{
	    		this.showInWebPane( "Could not load your URL:<br>" + url );
	    	}
	    }
	    
    	browserFrame.setCursor(Cursor.getDefaultCursor());
	}
	

	/**
	 * Extract links from a directory of such. Assume that links are enclosed in
	 * quotes and start with "http://", which seems to be the case for DODS directories.
	 * @param u the url of the directory.
	 * @return a string representation of links.
	 * TODO: think about more sophisticated versions (including a separate class).
	 * TODO: think about buffering, threading, etc., not to hang up the main browser.
	 */
	private String getWebLinks( URL u )
	{	
		// The buffer to store everything in.
		StringBuffer buffer = new StringBuffer();
		
		// try reading line by line and replacing the input.
		try
		{
			InputStream in = u.openStream();
			InputStreamReader r = new InputStreamReader(in);
			StreamTokenizer urls = new StreamTokenizer( r );
			int delimeter = '\"';
			
			// for now, retrieve only the first 40 links.
			int counter = 0;
			
			while( urls.nextToken() != StreamTokenizer.TT_EOF )
			{
				// see if we got a quoted string.
				if( urls.ttype == delimeter )
				{
					// see if we have a data URL, in which case add it to the buffer.
					if( urls.sval.startsWith( "http://" ) &
							(new TypedFile( urls.sval )).isData() )
					{
						buffer.append( linkTo( urls.sval ) );
						counter ++;
					}
				}
				
				// check the number of links; quit the loop if reached the upper limit.
				if( counter > 40 )
					break;
			}
		}
		// in case we fail, return.
		catch( IOException e )
		{
			return "";
		}
		
		return new String( buffer );
	}
	
	
	/**
	 * Generating HTML that can be used to link to the file.
	 * @param url fo the file to link.
	 * @return HTML suitable for displaying.
	 */
	private static String linkTo( String url )
	{
		return "<br><A HREF=\"" + url + "\">" + url + "</A>";
	}
	
	/*
	 * Generating HTML that can be used to link to the file.
	 * @param url fo the file to link.
	 * @param name to display.
	 * @return HTML suitable for displaying.
	 */
	/*
	private static String linkTo( String url, String name )
	{
		return "<br><A HREF=\"" + url + "\">" + name + "</A>";
	}
	*/
	
	
	/**
	 * Show a message in the web pane in response to a click.
	 * @param message to show.
	 */
	private void showInWebPane( String message )
	{
		this.webPane.setContentType( "text/html" );
		this.webPane.setText( "<html>" + message + "<html>" );
	}

	
	/**
	 * Get a URL object, hopefully non-null, out of a string.
	 * As URL's can come in different forms, try handling several.
	 * TODO: treat the case that the protocol (http://) is not specified.
	 * @param preURL
	 * @return
	 */
	private URL getURL( String preURL )
	{
		// if created succesffully, we have succeeded.
	    URL url = null;
    	String preURL1;
	    
	    // first, try it the way it is.
	    try
	    {
	    	// logger.debug( "Trying: " + preURL );
	    	url = new URL( preURL );
	    }
	    // here we treat all possible malformed URL's.
	    catch( MalformedURLException e )
	    {
	    	// check for an absolute url.
	    	if( preURL.startsWith( "/" ) )
	    	{
	    		preURL1 = this.URLprotocol + "://" + this.URLhost + preURL;
	    	}
	    	else
	    	{
	    		// here is a relative URL, with path possibly ending with "/"
	    		String separator = this.URLpath.endsWith( "/" ) ? "" : "/";
	    		preURL1 = this.URLprotocol + "://" + this.URLhost + this.URLpath 
	    			+ separator + preURL;
	    	}
	    	
	    	// try one of the two combinations.
	    	try
	    	{
		    	// logger.debug( "Trying: " + preURL1 );
		    	url = new URL( preURL1 );
	    	}
	    	catch( MalformedURLException e1 )
		   	{
	    		return null;
	    	}
	    }
			
	    // if the URL is normal by the end of this, can initialize things.
	    if( url != null )
	    {
	    	this.URLprotocol = url.getProtocol();
	    	this.URLhost = url.getHost();
	    	this.URLpath = url.getPath();
	    }
	    
	    // return whatever the result might be.
	    return url;
	}

	// ----------------------------------------------------------------------
	// Im2Learn Menu
	// ----------------------------------------------------------------------
    
    public void imageUpdated(ImageUpdateEvent event) {        
    }

    public void setImagePanel(ImagePanel imagepanel) {
        this.imagepanel = imagepanel;
    }

    public JMenuItem[] getPanelMenuItems() {
        return null;
    }

    public JMenuItem[] getMainMenuItems() {
        JMenu tool = new JMenu("File");
        tool.add(new JMenuItem(new AbstractAction("Load Albedo") {
            public void actionPerformed(ActionEvent e) {
                String url = "http://daac.gsfc.nasa.gov/services/dods/modis_terra_dp.shtml";
                Im2LearnMainFrame frame = (Im2LearnMainFrame) SwingUtilities.getWindowAncestor(imagepanel);
                SimpleBrowser browser = new SimpleBrowser(frame, url );
                String filename = browser.showDialog();
                if (filename != null) {
                    LoadSaveImagePanel.load(filename, imagepanel);
                }
            }
        }));
        return new JMenuItem[] { tool };
    }

    public URL getHelp(String topic) {
        return null;
    }	
}
