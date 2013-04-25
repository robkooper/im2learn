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
package edu.illinois.ncsa.isda.im2learn.ext.geo;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.border.*;

import edu.illinois.ncsa.isda.im2learn.core.datatype.*;
import edu.illinois.ncsa.isda.im2learn.core.display.*;
import edu.illinois.ncsa.isda.im2learn.core.io.*;


/**
 * UI for MosaicGeoTiles.
 * @author clutter
 * @todo add sampling
 */
public class MosaicUI extends JPanel {
  /** the mosaicked image */
  private ImageObject geoImg;
  /** the image panel to display the mosaicked image */
  private ImagePanel imagepanel;

  public MosaicUI() {
    JPanel p1 = new JPanel(new BorderLayout());
    p1.setBorder(new TitledBorder("Parameters"));

    // keep a jlist of the file names
    // these files will be mosaicked
    final DefaultListModel listModel = new DefaultListModel();
    final JList filelist = new JList(listModel);
    filelist.setVisibleRowCount(10);
    filelist.setPrototypeCellValue("this is a good lenght for the absolut path toa file");
    filelist.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

    JScrollPane scrollPane = new JScrollPane(filelist);
    scrollPane.setColumnHeaderView(new JLabel("Files to Load"));

    // add button to add file names to the jlist
    JButton addbutton = new JButton("Add");
    addbutton.addActionListener(new AbstractAction() {
      public void actionPerformed(ActionEvent ae) {
        JFileChooser jfc = new JFileChooser(FileChooser.getInitialDirectory());
        jfc.setMultiSelectionEnabled(true);

          int retval = jfc.showOpenDialog(MosaicUI.this);
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

    // remove button to remove a file name from the jlist
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

    // run button runs the mosaic process
    JPanel buttons = new JPanel();
    JButton run = new JButton("Run");
    final JButton show = new JButton("Show");
    run.addActionListener(new RunnableAction() {
      public void run() {
        String[] files = new String[listModel.getSize()];
        listModel.copyInto(files);

        // should probably put up some progress meter

        try {
          MosaicGeoTiles mosaic = new MosaicGeoTiles();
         
          geoImg = mosaic.mosaic(files, null);
          if(geoImg != null)
            show.setEnabled(true);
        }
        catch(Exception ex) {
          show.setEnabled(false);
          ex.printStackTrace();
        }
      }
    });

    // show button for showing the result
    show.setEnabled(false);
    show.addActionListener(new RunnableAction() {
      public void run() {
        if(geoImg != null)
          imagepanel.setImageObject(geoImg);
      }
    });

    buttons.add(run);
    buttons.add(show);

    Container c = this;
    c.setLayout(new BorderLayout());
    c.add(p1, BorderLayout.CENTER);
    c.add(buttons, BorderLayout.SOUTH);
  }

  public void setImagePanel(ImagePanel imagepanel) {
    this.imagepanel = imagepanel;
  }


  abstract class RunnableAction extends AbstractAction implements Runnable {
    protected ActionEvent event;
    public void actionPerformed(ActionEvent ae) {
      event = ae;
      SwingUtilities.invokeLater(this);
    }
  }

}
