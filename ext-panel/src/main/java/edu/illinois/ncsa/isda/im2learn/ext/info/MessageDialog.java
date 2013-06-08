package edu.illinois.ncsa.isda.im2learn.ext.info;

import java.awt.Frame;
import javax.swing.JOptionPane;

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



public class MessageDialog {

	final static public int Option_Yes = 1;
	final static public int Option_No = 0;
	final static public int Option_Cancel = -1;
	
  public MessageDialog() {}

  public static void displayMessage(String message) {
    Frame f = new Frame();
    displayMessage(message,f);
    f.dispose();
  }

  public static void displayMessage(String message, Frame f) {
    if(message != null)
      JOptionPane.showMessageDialog(f, message);
  }

  public static int YesNo(String message) {
    Frame f = new Frame();
    int ret = YesNo(message,f);
    f.dispose();
    return ret;
  }

  public static int YesNo(String message, Frame f) {
    if(message != null) {
      // Modal dialog with yes/no button
      int answer = JOptionPane.showConfirmDialog(f, message);
      if (answer == JOptionPane.YES_OPTION) {
        return Option_Yes;
      } else if (answer == JOptionPane.NO_OPTION) {
        return Option_No;
      }
      else {
        return Option_Cancel;
      }

    }
    return Option_Cancel;
  }


  public static String Input(String message) {
    Frame f = new Frame();
    String ret = Input(message,f);
    f.dispose();
    return ret;
  }

  public static String Input(String message, Frame f) {
    if(message != null) {
      return JOptionPane.showInputDialog(f, message);
    }
    return null;

  }

}

