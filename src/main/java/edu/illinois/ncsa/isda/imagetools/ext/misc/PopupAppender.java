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
package edu.illinois.ncsa.isda.imagetools.ext.misc;

import java.awt.GraphicsEnvironment;

import javax.swing.JOptionPane;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Show a popup with the event message. This class, if added to the logger, will
 * show a popup for any messages it receives. By default this will only show
 * error messages, but this can be changed by calling setThreshold() with the
 * appropriate level, however, if the root logger is set to error, this class
 * can only receive events of error or higher.
 * <p/>
 * If the event is in response to a exception thrown, the dialog will have as
 * title EXCEPTION followed by the message and the reason of the exception as
 * the message. If the event is not an exception the dialog will have as title
 * the level of the event, and the message specified.
 * <p/>
 * The icon shown with the message is based on the level of the event. Any
 * events of level error or higher, will have the error icon, any events of type
 * warn will have the warning icon, any info events will have the information
 * icon, and all others will have a plain icon.
 * 
 * @author Rob Kooper
 * @version 1.0
 */
public class PopupAppender extends AppenderSkeleton {
    /**
     * Default constructor, sets the threshold to error.
     */
    public PopupAppender() {
        setThreshold(Level.ERROR);
    }

    /**
     * Called if an event happened and the event has a level higher than the
     * threshold. Based on the event the right messageicon and popup is shown.
     * 
     * @param event
     *            that triggered the logging.
     */
    @Override
    protected void append(LoggingEvent event) {
        int eventlvl = event.getLevel().toInt();

        // get message.
        String message = event.getMessage().toString();

        // if there is a throwable set title and message, otherwise use
        // the as title the level of the event.
        String[] thr = event.getThrowableStrRep();
        String title = null;
        if (thr != null) {
            title = "EXCEPTION : " + message;
            message = thr[0];
        } else {
            title = event.getLevel().toString();
        }

        // based on level select icon
        int type = JOptionPane.PLAIN_MESSAGE;
        if (eventlvl >= Level.ERROR_INT) {
            type = JOptionPane.ERROR_MESSAGE;
        } else if (eventlvl >= Level.WARN_INT) {
            type = JOptionPane.WARNING_MESSAGE;
        } else if (eventlvl >= Level.INFO_INT) {
            type = JOptionPane.INFORMATION_MESSAGE;
        }

        // show the message
        if (!GraphicsEnvironment.isHeadless()) {
            JOptionPane.showMessageDialog(null, message, title, type);
        }
    }

    /**
     * Nothing to close.
     */
    public void close() {
    }

    /**
     * No need for any layout.
     */
    public boolean requiresLayout() {
        return false;
    }
}
