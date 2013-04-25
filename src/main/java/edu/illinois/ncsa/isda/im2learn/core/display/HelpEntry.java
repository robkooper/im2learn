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
package edu.illinois.ncsa.isda.im2learn.core.display;

import java.net.URL;

/**
 * Nodes for the Im2Learn helpsystem. This is a tree of items and will be merged at
 * runtime. Each class that implements Im2LearnMenu and is added to the menu of
 * Im2LearnMainFrame is automatically checked to see if it provides help. The help
 * can also be added by calling HelpViewer.addHelp().
 *
 * @author Rob Kooper
 * @version 1.1
 * @deprecated no longer used
 */
public interface HelpEntry {
    /**
     * Return a list of topics. Each topic can have multiple subtopics resulting
     * in a tree of help topics.
     *
     * @return list of helptopics this class can report on.
     */
    public HelpTopic[] getTopics();

    /**
     * Return a URL which describes the topic. The URL can specify an html page
     * that is stored with the class file. To support loading html pages from
     * a jar file it is best to use this.getClass().getResource("html file").
     * This will return a URL that is either to the html file on the filesystem
     * or a URL to the html file inside a jar file.
     *
     * @param topic the helptopic of which to return help.
     *
     * @return the url to the webpage with the help text.
     */
    public URL getHelp(String topic);

    /**
     * Tree structure of help topics.
     */
    class HelpTopic {
        public HelpTopic[] subtopic;
        public String topic;

        public HelpTopic(String topic) {
            this(topic, (HelpTopic[])null);
        }

        public HelpTopic(String topic, HelpTopic subtopic) {
            this(topic, new HelpTopic[]{subtopic});
        }

        public HelpTopic(String topic, HelpTopic[] subtopic) {
            this.topic = topic;
            this.subtopic = subtopic;
        }
    }
}
