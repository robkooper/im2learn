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
package edu.illinois.ncsa.isda.imagetools.ext.geo;

import java.io.*;


import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import edu.illinois.ncsa.isda.imagetools.core.datatype.*;
import edu.illinois.ncsa.isda.imagetools.core.display.*;
import edu.illinois.ncsa.isda.imagetools.core.io.*;

import java.awt.*;
import java.awt.event.*;

/**
 * UI cover for AlignGeoTiles.  Allows the user to add files and run the
 * AlignGeoTiles function.
 * @author clutter
 * @todo add sampling
 * @todo progress bar
 * @todo ability to cancel while running
 */
public class AlignUI extends JPanel {
  /** the image panel to display an image on */
  private ImagePanel imagepanel;
  /** the aligned stack of images */
  private ImageObject[] geoImg;

  public AlignUI() {
    JPanel p1 = new JPanel(new BorderLayout());
    p1.setBorder(new TitledBorder("Parameters"));

    // jlist for file names
    final DefaultListModel listModel = new DefaultListModel();
    final JList filelist = new JList(listModel);
    filelist.setVisibleRowCount(10);
    filelist.setPrototypeCellValue("this is a good lenght for the absolut path toa file");
    filelist.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

    JScrollPane scrollPane = new JScrollPane(filelist);
    scrollPane.setColumnHeaderView(new JLabel("Files to Load"));

    // add button to add files to the jlist
    JButton addbutton = new JButton("Add");
    addbutton.addActionListener(new AbstractAction() {
      public void actionPerformed(ActionEvent ae) {
        JFileChooser jfc = new JFileChooser(FileChooser.getInitialDirectory());
        jfc.setMultiSelectionEnabled(true);

          int retval = jfc.showOpenDialog(AlignUI.this);
          if(retval == JFileChooser.APPROVE_OPTION) {

            File[] files = jfc.getSelectedFiles();

            for(int i = 0; i < files.length; i++) {
              listModel.addElement(files[i].getAbsolutePath());
            }

            if(files.length > 0) {
              FileChooser.setInitialDirectory(files[0].getAbsolutePath());
            }
          }
      }
    });

    // remove button to remove files from the jlist
    JButton removebutton = new JButton("Remove");
    removebutton.addActionListener(new AbstractAction() {
      public void actionPerformed(ActionEvent ae) {
        Object[] selected = filelist.getSelectedValues();
        for(int i = 0; i < selected.length; i++) {
          listModel.removeElement(selected[i]);
        }
      }
    });

    JPanel p2 = new JPanel();
    p2.add(addbutton);
    p2.add(removebutton);

    p1.setLayout(new BorderLayout());
    p1.add(scrollPane, BorderLayout.CENTER);
    p1.add(p2, BorderLayout.SOUTH);

    final Thread runthread = new Thread() {
       public void run() {
         String[] files = new String[listModel.getSize()];
         listModel.copyInto(files);

         // should probably put up some progress meter
         //ProgressDialog dialog = new ProgressDialog();

         try {
           geoImg = AlignGeoTiles.align(files, null);
//           dialog.p
         }
         catch (Exception ex) {
           ex.printStackTrace();
         }
       }
     };

    JPanel buttons = new JPanel();
    JButton run = new JButton("Run");
    run.addActionListener(new AbstractAction() {
      public void actionPerformed(ActionEvent ae) {
        runthread.start();
      }
    });


/*    JButton show = new JButton("Show Selected");
    show.addActionListener(new RunnableAction() {
      public void run() {
        if(geoImg != null) {
          int selectedIndex = filelist.getSelectedIndex();
          if(selectedIndex != -1)
            imagepanel.setImageObject(geoImg[selectedIndex]);

          //imagepanel.setImageObject(geoImg);
        }
      }
    });*/

    filelist.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent lse) {
        if(!lse.getValueIsAdjusting()) {
          if(geoImg != null) {
            int idx = filelist.getSelectedIndex();
          if(idx != -1)
            imagepanel.setImageObject(geoImg[idx]);

          }
        }
      }
    });

    buttons.add(run);
    //buttons.add(show);

    Container c = this;
    c.setLayout(new BorderLayout());
    c.add(p1, BorderLayout.CENTER);
    c.add(buttons, BorderLayout.SOUTH);
  }

  public void setImagePanel(ImagePanel imagepanel) {
    this.imagepanel = imagepanel;
  }

  public ImageObject[] getAlignedImages() {
    return geoImg;
  }

  class ProgressDialog extends JDialog {
    JProgressBar progressBar;
    ProgressDialog() {
      super();
      progressBar = new JProgressBar(JProgressBar.HORIZONTAL);

      Container c = getContentPane();

      progressBar.setBorder(new EmptyBorder(20, 10, 20, 10));
      c.add(progressBar);
      progressBar.setIndeterminate(true);
    }
  }
}
